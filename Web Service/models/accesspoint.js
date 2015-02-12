// Load required packages
var mongoose = require('mongoose');
var ObjectId = mongoose.SchemaTypes.ObjectId;

// Define our AP schema
var AccessPointSchema = new mongoose.Schema({
  publisher: {
    type: ObjectId, 
    ref: 'User', 
    required: true
  },
  bssid: {
    type: String, 
    required: true,
  },
  name: {
    type: String, 
    required: true
  },
  password: {
    type: String, 
    required: true
  },
  securityType: {
    type: String, 
    required: true
  },
  privacyType: {
    type: Number, 
    required: true,
    default: 1
  },
  lat: {
    type: Number,
    required: true,
    // set: function(v) {}
  },
  lon: {
    type: Number,
    required: true
  },
  lastAccessed: {
    type: Date, 
    required: true,
    default: Date.now
  }
});

AccessPointSchema.pre("save", function(next) {   
  var ap = this;
  if(ap.privacyType != 1) {
    AccessPointModel.findOne({bssid : this.bssid}, 'bssid', function(err, results) {
        if(err) {
          console.log("AccessPoint findOne error " + err);
          next(err);
        } else if(results) {
          ap.invalidate("bssid", "BSSID must be unique");
          next(new Error("BSSID must be unique"));
        } else {
          console.log("AccessPoint findOne OK.");
          next();
        }
    });
  }
  else {
    AccessPointModel.findOne({publisher : this.publisher, bssid: this.bssid}, function(err, results) {
        if(err) {
          console.log("AccessPoint findOne error: " + err);
          next(err);
        } else if(results) {
          ap.invalidate("bssid", "BSSID must be unique");
          next(new Error("BSSID must be unique"));
        } else {
          console.log("AccessPoint findOne OK");
          next();
        }
    });
  }
});

var AccessPointModel = mongoose.model('AccessPoint', AccessPointSchema);
// Export the Mongoose model
module.exports = AccessPointModel;