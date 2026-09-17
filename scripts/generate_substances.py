import json
import urllib.request
import uuid
import re

URL = "https://raw.githubusercontent.com/TripSit/drugs/main/drugs.json"
OUTPUT_PATH = "app/src/main/assets/substances.json"

def parse_time(time_str):
    if not time_str: return 0.0
    # Simplistic parser for "X-Y hours" -> takes max
    # Real app would need a more robust parser
    match = re.search(r'([\d.]+)\s*-\s*([\d.]+)', time_str)
    if match:
        return float(match.group(2))
    match = re.search(r'([\d.]+)', time_str)
    if match:
        return float(match.group(1))
    return 0.0

def parse_dose(dose_str):
    if not dose_str: return None
    match = re.search(r'([\d.]+)\s*-\s*([\d.]+)', dose_str)
    if match:
        return float(match.group(1)) # Take lower bound for safety
    match = re.search(r'([\d.]+)', dose_str)
    if match:
        return float(match.group(1))
    return None

def main():
    print(f"Downloading data from {URL}...")
    req = urllib.request.urlopen(URL)
    data = json.loads(req.read().decode('utf-8'))
    
    substances = []
    
    for key, val in data.items():
        if "name" not in val: continue
        name = val["name"]
        aliases = val.get("aliases", [])
        
        roas = []
        if "formatted_dose" in val:
            for roa_name, dose_info in val["formatted_dose"].items():
                
                dose = None
                if isinstance(dose_info, dict):
                    dose = {
                        "threshold": parse_dose(dose_info.get("Threshold")),
                        "light": parse_dose(dose_info.get("Light")),
                        "common": parse_dose(dose_info.get("Common")),
                        "strong": parse_dose(dose_info.get("Strong")),
                        "heavy": parse_dose(dose_info.get("Heavy"))
                    }

                duration_info = val.get("formatted_duration", {}).get(roa_name, {})
                duration = None
                if isinstance(duration_info, dict):
                    duration = {
                        "onset": parse_time(duration_info.get("Onset")),
                        "comeup": parse_time(duration_info.get("Come_up")),
                        "peak": parse_time(duration_info.get("Peak")),
                        "offset": parse_time(duration_info.get("Offset")),
                        "afterglow": parse_time(duration_info.get("Afterglow"))
                    }
                
                roas.append({
                    "name": roa_name,
                    "dose": dose,
                    "duration": duration
                })
        
        interactions = []
        if "interactions" in val:
            for interaction in val["interactions"]:
                interactions.append({
                    "substanceId": interaction.get("name", "Unknown").lower().replace(" ", "-"),
                    "status": interaction.get("status", "Unknown"),
                    "notes": interaction.get("note")
                })
                
        substances.append({
            "id": name.lower().replace(" ", "-"),
            "name": name,
            "aliases": aliases,
            "roas": roas,
            "interactions": interactions
        })
        
    print(f"Parsed {len(substances)} substances.")
    
    with open(OUTPUT_PATH, "w") as f:
        json.dump(substances, f, indent=2)
    print(f"Saved to {OUTPUT_PATH}")

if __name__ == "__main__":
    main()
