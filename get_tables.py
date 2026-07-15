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
        print(f"❌ Error running query: {query}")
        print(f"HTTP Code: {e.code}")
        try:
            error_body = e.read().decode("utf-8")
            print(f"Error Body: {error_body}")
        except Exception as read_err:
            print(f"Could not read error body: {read_err}")
        return None

def main():
    print("--- 📋 GETTING ALL TABLES ---")
    
    # Get all tables
    query = """
        SELECT table_name 
        FROM information_schema.tables 
        WHERE table_schema = 'public'
        ORDER BY table_name
    """
    
    result = execute_query(query)
    if result and "rows" in result:
        tables = [row["table_name"] for row in result["rows"]]
        print(f"✅ Found {len(tables)} tables:")
        for i, table in enumerate(tables, 1):
            print(f"{i}. {table}")
        
        # Save to file
        with open("tables_list.txt", "w") as f:
            for table in tables:
                f.write(f"{table}\n")
        print(f"\n✅ Table list saved to tables_list.txt")
    else:
        print("❌ Failed to get tables")

if __name__ == "__main__":
    main()
