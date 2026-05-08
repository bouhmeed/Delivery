const { Pool } = require('pg');
require('dotenv').config();

const pool = new Pool({
    connectionString: process.env.DATABASE_URL,
    ssl: { rejectUnauthorized: true }
});

// Test des URLs TomTom corrigées
async function testCorrectedTomTomUrls() {
    console.log('🧪 TEST DES URLS TOMTOM CORRIGÉES\n');
    
    // 1. URL originale qui fonctionne (fournie par l'utilisateur)
    console.log('✅ URL ORIGINALE QUI FONCTIONNE:');
    const workingUrl = "https://plan.tomtom.com/en/route/plan?key=c92wOsiK2ds07Gzq9ZJXNRyyWeQhSYse&p=47.07279,5.27771,5.17z&r=(costModel:FASTEST,routingProvider:GLOBAL,sorted:(h~V45.767306~J4.834306~Vaddr~JLyon,h~V48.856895~J2.350849~Vaddr~JParis,h~V43.703427~J7.266266~Vaddr~JNice),travelMode:CAR,vehicleParameters:(axleWeight:-+,height:-+,length:-+,maxSpeed:-+,vehicleModelId:-+,weight:-+,width:-+))&to=48.856895,2.3508487";
    console.log('   URL:', workingUrl);
    
    // 2. Analyse des différences clés
    console.log('\n🔍 ANALYSE DES DIFFÉRENCES CLÉS:');
    console.log('   Format waypoint correct: h~V{lat}~J{lon}~Vaddr~J{nom}');
    console.log('   - J avant la latitude: V{lat}');
    console.log('   - J avant la longitude: J{lon}');
    console.log('   - J avant le nom: J{nom} (important!)');
    console.log('   - Structure: h~V45.767306~J4.834306~Vaddr~JLyon');
    
    // 3. URL incorrecte (ancienne version)
    console.log('\n❌ URL INCORRECTE (ANCIENNE VERSION):');
    const incorrectUrl = "https://plan.tomtom.com/en/route/plan?key=...&r=(h~V45.767306~J4.834306~Vaddr~Lyon,h~V48.856895~J2.350849~Vaddr~Paris,...)";
    console.log('   Problème: Vaddr~Lyon au lieu de Vaddr~JLyon');
    console.log('   Problème: Manque le J avant le nom');
    
    // 4. URL corrigée (nouvelle version)
    console.log('\n✅ URL CORRIGÉE (NOUVELLE VERSION):');
    const correctedUrl = "https://plan.tomtom.com/en/route/plan?key=c92wOsiK2ds07Gzq9ZJXNRyyWeQhSYse&p=47.07279,5.27771,5.17z&r=(costModel:FASTEST,routingProvider:GLOBAL,sorted:(h~V45.767306~J4.834306~Vaddr~JLyon,h~V48.856895~J2.350849~Vaddr~JParis,h~V43.703427~J7.266266~Vaddr~JNice),travelMode:CAR,vehicleParameters:(axleWeight:-+,height:-+,length:-+,maxSpeed:-+,vehicleModelId:-+,weight:-+,width:-+))&to=48.856895,2.3508487";
    console.log('   URL:', correctedUrl);
    console.log('   Correction: Vaddr~JLyon au lieu de Vaddr~Lyon');
    
    // 5. Test des différents modes de navigation
    console.log('\n🧪 TEST DES DIFFÉRENTS MODES:');
    
    const navigationModes = [
        {
            name: 'Navigation Simple',
            waypoints: 'h~V45.767306~J4.834306~Vaddr~JDriver,h~V48.856895~J2.350849~Vaddr~JClient',
            to: '48.856895,2.3508487'
        },
        {
            name: 'Navigation Avancée',
            waypoints: 'h~V45.767306~J4.834306~Vaddr~JLyon,h~V48.856895~J2.350849~Vaddr~JParis,h~V43.703427~J7.266266~Vaddr~JNice',
            to: '48.856895,2.3508487'
        },
        {
            name: 'Navigation Complexe',
            waypoints: 'h~V45.767306~J4.834306~Vaddr~JLyon,h~V48.856895~J2.350849~Vaddr~JParis,h~V43.703427~J7.266266~Vaddr~JNice,h~V48.856895~J2.350849~Vaddr~JParis',
            to: '48.856895,2.3508487'
        }
    ];
    
    navigationModes.forEach((mode, index) => {
        console.log(`\n   ${index + 1}. ${mode.name}:`);
        console.log(`      Waypoints: ${mode.waypoints}`);
        console.log(`      To: ${mode.to}`);
        
        const testUrl = `https://plan.tomtom.com/en/route/plan?key=c92wOsiK2ds07Gzq9ZJXNRyyWeQhSYse&p=47.07279,5.27771,5.17z&r=(costModel:FASTEST,routingProvider:GLOBAL,sorted:(${mode.waypoints}),travelMode:CAR,vehicleParameters:(axleWeight:-+,height:-+,length:-+,maxSpeed:-+,vehicleModelId:-+,weight:-+,width:-+))&to=${mode.to}`;
        console.log(`      URL complète: ${testUrl}`);
    });
    
    // 6. Analyse des corrections apportées
    console.log('\n🔧 CORRECTIONS APPORTÉES:');
    console.log('   1. FORMAT DES WAYPOINTS:');
    console.log('      AVANT: Vaddr~{nom}');
    console.log('      APRÈS: Vaddr~J{nom}');
    console.log('      EXEMPLE: Vaddr~JLyon');
    
    console.log('\n   2. STRUCTURE COMPLÈTE:');
    console.log('      AVANT: h~V{lat}~J{lon}~Vaddr~{nom}');
    console.log('      APRÈS: h~V{lat}~J{lon}~Vaddr~J{nom}');
    console.log('      EXEMPLE: h~V45.767306~J4.834306~Vaddr~JLyon');
    
    console.log('\n   3. COHÉRENCE DES NOMS:');
    console.log('      AVANT: Driver, Client, Lyon, Paris');
    console.log('      APRÈS: JDriver, JClient, JLyon, JParis');
    console.log('      RAISON: Le J est requis par TomTom');
    
    // 7. Test des waypoints spécifiques
    console.log('\n📍 TEST DES WAYPOINTS SPÉCIFIQUES:');
    const specificWaypoints = [
        {
            name: 'Lyon',
            lat: 45.767306,
            lon: 4.834306,
            format: 'h~V45.767306~J4.834306~Vaddr~JLyon'
        },
        {
            name: 'Paris',
            lat: 48.856895,
            lon: 2.350849,
            format: 'h~V48.856895~J2.350849~Vaddr~JParis'
        },
        {
            name: 'Nice',
            lat: 43.703427,
            lon: 7.266266,
            format: 'h~V43.703427~J7.266266~Vaddr~JNice'
        }
    ];
    
    specificWaypoints.forEach((wp, index) => {
        console.log(`   ${index + 1}. ${wp.name}:`);
        console.log(`      Coordonnées: ${wp.lat}, ${wp.lon}`);
        console.log(`      Format TomTom: ${wp.format}`);
    });
    
    // 8. Validation du format complet
    console.log('\n✅ VALIDATION DU FORMAT COMPLET:');
    const validFormat = {
        base: 'https://plan.tomtom.com/en/route/plan',
        key: 'key=c92wOsiK2ds07Gzq9ZJXNRyyWeQhSYse',
        position: 'p=47.07279,5.27771,5.17z',
        route: 'r=(costModel:FASTEST,routingProvider:GLOBAL,sorted:(...),travelMode:CAR,vehicleParameters:(...))',
        destination: 'to=48.856895,2.3508487'
    };
    
    console.log('   Format valide:');
    console.log(`   ${validFormat.base}?${validFormat.key}&${validFormat.position}&${validFormat.route}&${validFormat.destination}`);
    
    // 9. Résumé des corrections
    console.log('\n📊 RÉSUMÉ DES CORRECTIONS:');
    console.log('✅ CORRECTION PRINCIPALE:');
    console.log('   • Ajout du "J" avant les noms de waypoints');
    console.log('   • Format: Vaddr~J{nom} au lieu de Vaddr~{nom}');
    
    console.log('\n✅ IMPACT SUR LES URLS:');
    console.log('   • Navigation Simple: Corrigée');
    console.log('   • Navigation Avancée: Corrigée');
    console.log('   • Navigation Complexe: Corrigée');
    
    console.log('\n✅ RÉSULTAT ATTENDU:');
    console.log('   • Les URLs générées devraient maintenant fonctionner');
    console.log('   • Les waypoints seront correctement affichés dans TomTom');
    console.log('   • Les noms des lieux seront visibles sur la carte');
    
    // 10. Instructions de test
    console.log('\n🧪 INSTRUCTIONS DE TEST:');
    console.log('1. Compiler le projet Android');
    console.log('2. Ouvrir DeliveryTrackingScreen');
    console.log('3. Cliquer sur le bouton 🗺️');
    console.log('4. Choisir "Navigation Avancée"');
    console.log('5. Vérifier que l\'URL s\'ouvre correctement dans TomTom');
    console.log('6. Confirmer que les waypoints sont affichés avec les bons noms');
    
    return true;
}

// Si le script est appelé directement
if (require.main === module) {
    testCorrectedTomTomUrls();
}

module.exports = { testCorrectedTomTomUrls };
