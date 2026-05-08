const https = require('https');

// Configuration TomTom
const API_KEY = 'c92wOsiK2ds07Gzq9ZJXNRyyWeQhSYse';
const BASE_URL = 'https://api.tomtom.com';

// Test pour l'expédition EXP-2026-520
async function testTomTomRoutingAPI() {
    console.log('🚀 Test TomTom Routing API pour EXP-2026-520');
    
    try {
        // Coordonnées de test (Lyon vers Paris)
        const originLat = 45.764043;
        const originLon = 4.835659;
        const destLat = 48.856614;
        const destLon = 2.352222;
        
        // URL de l'API TomTom Routing
        const url = `${BASE_URL}/routing/1/calculateRoute/${originLat},${originLon}:${destLat},${destLon}/json?key=${API_KEY}`;
        
        console.log(`🔗 URL: ${url}`);
        console.log(`📍 Route: Lyon (${originLat}, ${originLon}) → Paris (${destLat}, ${destLon})`);
        
        // Faire la requête
        const response = await new Promise((resolve, reject) => {
            https.get(url, (res) => {
                let data = '';
                res.on('data', (chunk) => {
                    data += chunk;
                });
                res.on('end', () => {
                    try {
                        const json = JSON.parse(data);
                        resolve(json);
                    } catch (e) {
                        reject(e);
                    }
                });
            }).on('error', (err) => {
                reject(err);
            });
        });
        
        console.log('✅ Réponse reçue:');
        console.log(JSON.stringify(response, null, 2));
        
        // Analyser la réponse
        if (response.routes && response.routes.length > 0) {
            const route = response.routes[0];
            const summary = route.summary;
            
            console.log('\n📊 RÉSULTATS:');
            console.log(`   Distance: ${(summary.lengthInMeters / 1000).toFixed(2)} km`);
            console.log(`   Durée: ${Math.round(summary.travelTimeInSeconds / 60)} minutes`);
            console.log(`   Délai: ${Math.round(summary.trafficDelayInSeconds / 60)} minutes (trafic)`);
            
            // Comparaison avec la base de données
            console.log('\n🔍 COMPARAISON:');
            console.log('   Distance API TomTom:', (summary.lengthInMeters / 1000).toFixed(2), 'km');
            console.log('   Distance DB EXP-2026-520: [À vérifier dans la base]');
            
        } else {
            console.log('❌ Aucun itinéraire trouvé');
        }
        
    } catch (error) {
        console.error('❌ Erreur:', error.message);
    }
}

// Test de géocodage pour EXP-2026-520
async function testGeocoding() {
    console.log('\n🗺️ Test Géocodage pour EXP-2026-520');
    
    try {
        // Adresses de test (à adapter avec les vraies adresses de EXP-2026-520)
        const originAddress = 'Lyon, France';
        const destAddress = 'Paris, France';
        
        const originUrl = `${BASE_URL}/search/2/geocode/${encodeURIComponent(originAddress)}.json?key=${API_KEY}`;
        const destUrl = `${BASE_URL}/search/2/geocode/${encodeURIComponent(destAddress)}.json?key=${API_KEY}`;
        
        console.log(`🔍 Géocodage origine: ${originAddress}`);
        console.log(`🔍 Géocodage destination: ${destAddress}`);
        
        const [originResult, destResult] = await Promise.all([
            new Promise((resolve, reject) => {
                https.get(originUrl, (res) => {
                    let data = '';
                    res.on('data', (chunk) => data += chunk);
                    res.on('end', () => resolve(JSON.parse(data)));
                }).on('error', reject);
            }),
            new Promise((resolve, reject) => {
                https.get(destUrl, (res) => {
                    let data = '';
                    res.on('data', (chunk) => data += chunk);
                    res.on('end', () => resolve(JSON.parse(data)));
                }).on('error', reject);
            })
        ]);
        
        console.log('\n📍 RÉSULTATS GÉOCODAGE:');
        
        if (originResult.results && originResult.results.length > 0) {
            const origin = originResult.results[0];
            console.log(`   Origine: ${origin.position.lat}, ${origin.position.lon}`);
            console.log(`   Adresse: ${origin.address.freeformAddress}`);
        }
        
        if (destResult.results && destResult.results.length > 0) {
            const dest = destResult.results[0];
            console.log(`   Destination: ${dest.position.lat}, ${dest.position.lon}`);
            console.log(`   Adresse: ${dest.address.freeformAddress}`);
        }
        
        // Utiliser ces coordonnées pour le test de routing
        if (originResult.results && destResult.results && 
            originResult.results.length > 0 && destResult.results.length > 0) {
            
            const originPos = originResult.results[0].position;
            const destPos = destResult.results[0].position;
            
            console.log('\n🚀 Test routing avec coordonnées géocodées:');
            await testTomTomRoutingWithCoords(originPos.lat, originPos.lon, destPos.lat, destPos.lon);
        }
        
    } catch (error) {
        console.error('❌ Erreur géocodage:', error.message);
    }
}

// Test routing avec coordonnées spécifiques
async function testTomTomRoutingWithCoords(originLat, originLon, destLat, destLon) {
    try {
        const url = `${BASE_URL}/routing/1/calculateRoute/${originLat},${originLon}:${destLat},${destLon}/json?key=${API_KEY}`;
        
        console.log(`🔗 URL: ${url}`);
        
        const response = await new Promise((resolve, reject) => {
            https.get(url, (res) => {
                let data = '';
                res.on('data', (chunk) => data += chunk);
                res.on('end', () => {
                    try {
                        const json = JSON.parse(data);
                        resolve(json);
                    } catch (e) {
                        reject(e);
                    }
                });
            }).on('error', (err) => {
                reject(err);
            });
        });
        
        if (response.routes && response.routes.length > 0) {
            const route = response.routes[0];
            const summary = route.summary;
            
            console.log('\n📊 ROUTE AVEC COORDONNÉES GÉOCODÉES:');
            console.log(`   Distance: ${(summary.lengthInMeters / 1000).toFixed(2)} km`);
            console.log(`   Durée: ${Math.round(summary.travelTimeInSeconds / 60)} minutes`);
        }
        
    } catch (error) {
        console.error('❌ Erreur routing:', error.message);
    }
}

// Exécuter les tests
async function runTests() {
    console.log('🧪 DÉBUT DES TESTS TOMTOM API - EXP-2026-520');
    console.log('=' .repeat(60));
    
    // Test 1: Routing avec coordonnées connues
    await testTomTomRoutingAPI();
    
    // Test 2: Géocodage + routing
    await testGeocoding();
    
    console.log('\n✅ TESTS TERMINÉS');
}

runTests().catch(console.error);
