import urllib.request
import urllib.error
import json
import sys

NEON_SQL_ENDPOINT = "https://ep-cold-glade-a863rafo-pooler.eastus2.azure.neon.tech/sql"
NEON_CONNECTION_STRING = "postgresql://neondb_owner:npg_xmZ2G7KltoCN@ep-cold-glade-a863rafo-pooler.eastus2.azure.neon.tech/neondb?sslmode=require"

if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')

def execute_query(query):
    req = urllib.request.Request(
        NEON_SQL_ENDPOINT,
        data=json.dumps({"query": query}).encode("utf-8"),
        headers={
            "Content-Type": "application/json",
            "neon-connection-string": NEON_CONNECTION_STRING
        },
        method="POST"
    )
    try:
        with urllib.request.urlopen(req) as response:
            res_body = response.read().decode("utf-8")
            res_json = json.loads(res_body)
            return res_json
    except urllib.error.HTTPError as e:
        print(f"❌ Error running query")
        print(f"HTTP Code: {e.code}")
        try:
            error_body = e.read().decode("utf-8")
            print(f"Error Body: {error_body}")
        except Exception as read_err:
            print(f"Could not read error body: {read_err}")
        return None

def get_table_structure(table_name):
    print(f"\n--- 📊 TABLE: {table_name} ---")
    
    # Get column information
    query = f"""
        SELECT 
            column_name,
            data_type,
            is_nullable,
            column_default,
            character_maximum_length,
            numeric_precision,
            numeric_scale
        FROM information_schema.columns
        WHERE table_name = '{table_name}'
        ORDER BY ordinal_position
    """
    
    result = execute_query(query)
    if result and "rows" in result:
        print(f"Columns ({len(result['rows'])}):")
        for col in result['rows']:
            nullable = "NULL" if col['is_nullable'] == 'YES' else "NOT NULL"
            default = f" DEFAULT {col['column_default']}" if col['column_default'] else ""
            print(f"  - {col['column_name']}: {col['data_type']}{nullable}{default}")
    else:
        print("❌ Failed to get column information")
    
    # Get sample data
    sample_query = f'SELECT * FROM "{table_name}" LIMIT 3'
    sample_result = execute_query(sample_query)
    if sample_result and "rows" in sample_result:
        print(f"\nSample data ({len(sample_result['rows'])} rows):")
        for i, row in enumerate(sample_result['rows'], 1):
            print(f"  Row {i}: {json.dumps(row, indent=4, default=str)}")
    else:
        print("\nNo sample data available")

def main():
    tables = ["Vehicle", "VehicleCategory", "VehicleMaintenance"]
    
    for table in tables:
        get_table_structure(table)
    
    print("\n✅ Exploration complete!")

if __name__ == "__main__":
    main()
