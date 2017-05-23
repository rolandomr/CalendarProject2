// Import the Firebase SDK for Google Cloud Functions.
const functions = require('firebase-functions');
// Import and initialize the Firebase Admin SDK.
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

// Adds a message that welcomes new users into the chat.
exports.addWelcomeMessages = functions.auth.user().onCreate((event) => {
  const user = event.data;
  //this is the stackoverflow solution
//var user = firebase.auth().currentUser;

// user.updateProfile({
//   displayName: vm.form.username
// }).then(function(response) {
//   //Success
// }, function(error) {
//   //Error
//   console.log(error);
// });
//end of solution

  console.log("User created: " + JSON.stringify(event));

  //console.log('A new user signed in for the first time.');
  const fullName = user.displayName || 'Anonymous';
  event.data.displayName
  const userID = user.uid;
  const realName = user.displayName;
  // Saves the new welcome message into the database
  // which then displays it in the FriendlyChat clients.
  //return admin.database().ref('whatevs').child(userID).set({
  console.log('userID is ' + userID+ ' and realName is ' + realName);

  return admin.database().ref('whatevs/'+ userID).set({
  //return admin.database().ref('whatevs').set({
    //name: realName,NEED RETESTING BECAUSE OF BUG
    name : fullName,
    number_id: userID,
  });
  // return admin.database().ref('testingNewUsers').push({
  //   name: fullName,
  //   number_id: userID,
  //   //photoUrl: '/assets/images/firebase-logo.png', // Firebase logo
  //   //text: `${fullName} signed in for the first time! Welcome!`
  // });
});

exports.createFullCalendar = functions.database
    .ref('workers/{worker_id}/workInts')
    .onWrite(event => {
      //const post = event.data.val();
      //const previousData = event.data.previous.val();
      
      //overriding the previous data in the object
      //post.tittle = sanitize(post.tittle)
      //finally write the post back to the location where it came from
      //event.data.ref.set(post)
      //const workingdays = post.workInts;//this is already a list, not a JSON   
      //const workingdaysPrevious = previousData.workInts;
      //console.log("Before we had this " + workingdaysPrevious);
      //console.log("Now we have this " + workingdays);
      //var currentList = [];
      //var previousList = [];
      //console.log(post);
      //convert to LIST
      /*for (var key in workingdays) {
        if (workingdays.hasOwnProperty(key)) {
          //console.log(key + " -> " + workingdays[key]);
          currentList.push(workingdays[key]);
          //console.log("Current list in for loop "+currentList);
        } 
      }
      for (var key in workingdaysPrevious) {
        if (workingdays.hasOwnProperty(key)) {
          //console.log(key + " -> " + workingdays[key]);
          previousList.push(workingdaysPrevious[key]);
          //console.log("Previous list in for loop "+previousList);
        } 
      }*/
      //var added = getAddedElements(workingdaysPrevious, workingdays);

      var added = getAddedElements(event.data.previous.val(),event.data.val());
      //workingdays = post.workInts;
      //console.log("After getAddedElements workingdays: "+workingdays);
      //console.log("And workingdaysPrevious: "+workingdaysPrevious);
      //var removed = getRemovedElements(workingdaysPrevious,workingdays);
      var removed = getRemovedElements(event.data.previous.val(),event.data.val());
      //console.log("Before we had this " + previousList);
      //console.log("Now we have this " + currentList);
      console.log("These have been added " + added);
      console.log("These have been removed " + removed);
      //will have to add the new elements to the globalCalendar
      //and remove the ones that were taken from the worker's calendar
    });

    
function getAddedElements(previous,newShifts){
      var addedShifts = newShifts;
      //console.log("This are the added shifts "+addedShifts);
      //for (var i = 0; i < newShifts.length;i++) {
    for (var i = newShifts.length-1; i >= 0 ;i--) {
        if (contains(previous,newShifts[i])) {
            //console.log("Elemento ")
          addedShifts.splice(i,1);
        }
      }
      return addedShifts;
    }



    function getRemovedElements(previous,newShifts) {
      var removedShifts = previous;
    //console.log("This are the old shifts "+removedShifts);
    //console.log("And the new "+newShifts);
      for (var i = previous.length; i >=0 ;i--) {
        if (contains(newShifts,previous[i])) {
          removedShifts.splice(i,1);
          //console.log("Inside loop removed now has "+removedShifts);
        }
      }
      return removedShifts;
    }

    //Check if element is in list
    function contains(a, obj) {
    for (var i = 0; i < a.length; i++) {
        if (a[i] === obj) {
            return true;
        }
    }
    return false;
}