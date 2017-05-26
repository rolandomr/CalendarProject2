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
      if (event.data.previous.val() == null) {
        console.log("There were no shifts set before for this worker");
        admin.database().ref('generalCalendar').set(event.data.val());
        return; 
      } else {

      if (event.data.val() == null) {
        //this means that all days have been removed from user's calendar
        //need to remove the previous dates from the global calendar
        //in this case the removedelements are the one in event.data.previous
        //and there is no need to getRemovedElements
      var added = [];
      var removed = event.data.previous.val();
    } else{//there are actually shifts in the calendar
      var removed = getRemovedElements(event.data.previous.val(),event.data.val());
      var added = getAddedElements(event.data.previous.val(),event.data.val());
    }
      
      //var removed = getRemovedElements(event.data.previous.val(),event.data.val());

      console.log("These have been added " + added);
      console.log("These have been removed " + removed);
      //will have to add the new elements to the globalCalendar
      //and remove the ones that were taken from the worker's calendar
      //global calendar
      //will have to write to the database, i'm thinking in two places
      //one list for the shifts and one for the workers in a calendar

      return admin.database().ref('generalCalendar').once('value').then(snap =>{
          const alltheDays = snap.val();
          console.log("alltheDays are: "+alltheDays);
          removeThese(alltheDays,removed);
          console.log("alltheDays after removeThese are: "+alltheDays);
          for (var key in added) {
                  if (alltheDays.hasOwnProperty(key)) {
                    alltheDays.push(added[key]);
                    }
                }
          console.log("After the for loop alltheDays is: "+alltheDays);
          admin.database().ref('generalCalendar').set(alltheDays);
          return; 
      });
      }
      
      
      
      
      
      /*var alldays = admin.database().ref('generalCalendar/').once('value')
                      .then(function(dataSnapshot) {
                //handle read data
                console.log("This is all there is "+alldays);
                console.log("dataSnapshot.val() "+dataSnapshot.val());
                var auxdays = removeThese(dataSnapshot, removed);
                console.log("After removeThese auxdays is: "+auxdays);
                for (var key in added) {
                  if (auxdays.hasOwnProperty(key)) {
                    auxdays.push(added[key]);
                    }
                }
                console.log("After the for loop auxdays is: "+auxdays);
                return admin.database().ref('generalCalendar/').set(auxdays);  

         });*/
      
      //should use promises with then. and a last catch to log the errors

      
      //if i get the list, i will loop and remove the removed ones
      //then i will add the added ones and then reset the whole list of days
      //return admin.database().ref('generalCalendar/').set(added);  
    });
function removeThese(all, deleted) {
  var allMinusDeleted = all;
  for (var i = deleted.length;i >=0;i--) {
    if (contains(all,deleted[i])) {
      allMinusDeleted.splice(i,1);
    }
  }
  console.log("allMinusDelete is: "+allMinusDeleted);
  return allMinusDeleted;
}




    
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