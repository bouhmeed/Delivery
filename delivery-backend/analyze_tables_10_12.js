const { Pool } = require('pg');

const pool = new Pool({
  connectionString: "postgresql://neondb_owner:npg_xmZ2G7KltoCN@ep-cold-glade-a863rafo-pooler.eastus2.azure.neon.tech/neondb?sslmode=require&channel_binding=require",
  ssl: {
    rejectUnauthorized: false
  }
});

async function analyzeTable(tableName) {
  console.log(`\n=== TABLE: ${tableName} ===`);
  
  try {
    // Get table columns
    const columnsRes = await pool.query(`
      SELECT 
        column_name, 
        data_type, 
        is_nullable,
        column_default,
        character_maximum_length,
        numeric_precision,
        numeric_scale
      FROM information_schema.columns 
      WHERE table_name = $1 AND table_schema = 'public'
      ORDER BY ordinal_position
    `, [tableName]);
    
    console.log('\n--- COLUMNS ---');
    columnsRes.rows.forEach(col => {
      console.log(`${col.column_name}: ${col.data_type}${col.character_maximum_length ? `(${col.character_maximum_length})` : ''} ${col.is_nullable === 'NO' ? 'NOT NULL' : 'NULL'}${col.column_default ? ` DEFAULT ${col.column_default}` : ''}`);
    });

    // Get primary keys
    const pkRes = await pool.query(`
      SELECT a.attname
      FROM pg_index i
      JOIN pg_attribute a ON a.attrelid = i.indrelid AND a.attnum = ANY(i.indkey)
      WHERE i.indrelid = '"${tableName}"'::regclass AND i.indisprimary
    `);
    
    if (pkRes.rows.length > 0) {
      console.log('\n--- PRIMARY KEY ---');
      console.log(pkRes.rows.map(row => row.attname).join(', '));
    }

    // Get foreign keys
    const fkRes = await pool.query(`
      SELECT
        tc.constraint_name,
        kcu.column_name,
        ccu.table_name AS foreign_table_name,
        ccu.column_name AS foreign_column_name
      FROM information_schema.table_constraints AS tc
      JOIN information_schema.key_column_usage AS kcu
        ON tc.constraint_name = kcu.constraint_name
        AND tc.table_schema = kcu.table_schema
      JOIN information_schema.constraint_column_usage AS ccu
        ON ccu.constraint_name = tc.constraint_name
        AND ccu.table_schema = tc.table_schema
      WHERE tc.constraint_type = 'FOREIGN KEY'
        AND tc.table_name = $1
    `, [tableName]);
    
    if (fkRes.rows.length > 0) {
      console.log('\n--- FOREIGN KEYS ---');
      fkRes.rows.forEach(fk => {
        console.log(`${fk.column_name} -> ${fk.foreign_table_name}.${fk.foreign_column_name}`);
      });
    }

    // Get indexes
    const indexRes = await pool.query(`
      SELECT 
        indexname, 
        indexdef
      FROM pg_indexes 
      WHERE tablename = $1 AND schemaname = 'public'
      ORDER BY indexname
    `, [tableName]);
    
    if (indexRes.rows.length > 0) {
      console.log('\n--- INDEXES ---');
      indexRes.rows.forEach(idx => {
        console.log(`${idx.indexname}: ${idx.indexdef}`);
      });
    }

    // Get sample data
    const sampleRes = await pool.query(`SELECT * FROM "${tableName}" LIMIT 5`);
    if (sampleRes.rows.length > 0) {
      console.log('\n--- SAMPLE DATA (5 records) ---');
      sampleRes.rows.forEach((row, i) => {
        console.log(`Record ${i + 1}:`, JSON.stringify(row, null, 2));
      });
    } else {
      console.log('\n--- SAMPLE DATA ---');
      console.log('No data found');
    }

  } catch (err) {
    console.error(`Error analyzing ${tableName}:`, err.message);
  }
}

async function main() {
  const nextTables = ['Order', 'OrderItem', 'Shipment'];
  
  for (const table of nextTables) {
    await analyzeTable(table);
  }
  
  await pool.end();
}

main();
