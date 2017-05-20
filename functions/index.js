// Import the Firebase SDK for Google Cloud Functions.
const functions = require('firebase-functions');
// Import and initialize the Firebase Admin SDK.
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

// Adds a message that welcomes new users into the chat.
exports.addWelcomeMessages = functions.auth.user().onCreate((event) => {
  const user = event.data;
  console.log('A new user signed in for the first time.');
  const fullName = user.displayName || 'Anonymous';
  
  const userID = user.uid;
  // Saves the new welcome message into the database
  // which then displays it in the FriendlyChat clients.
  return admin.database().ref('whatevs').set({

  })
  return admin.database().ref('testingNewUsers').push({
    name: fullName,
    number_id: userID,
    //photoUrl: '/assets/images/firebase-logo.png', // Firebase logo
    //text: `${fullName} signed in for the first time! Welcome!`
  });
});