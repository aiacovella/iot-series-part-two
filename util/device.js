var mqtt = require('mqtt');
var util = require('util');
var client = mqtt.connect('mqtt://127.0.0.1');

var data = 0;
var interval = 500;

var deviceId  = "devices/mock/"

client.on('connect', function () {
    console.log('Connected to MQTT');
});

setInterval(function() {

    var callback = function() {
        console.log(util.format("Sent %s to %s", data, deviceId));
    };

    data = data + 1;

    client.publish(deviceId, data.toString(), {}, callback);

}, interval);
