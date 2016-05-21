// This file generates a bunch of random data
// It would be better to have separate files for each sensor 

var mqtt = require('mqtt');
var util = require('util');
//var client = mqtt.connect('mqtt://localhost', {username:"yourUser", password: "yourPassword"});
var client = mqtt.connect('mqtt://localhost');
var interval = 500;
var dht22Interval = 2000;

// Integer Topic
var topics = [
    "devices/demo/ultrasonic/0",
    "devices/demo/ultrasonic/1",
    "devices/demo/ultrasonic/2",
    "devices/demo/yun/red",
    "devices/demo/yun/green",
    "devices/demo/yun/blue",
    "devices/demo/yun/light",
    "devices/demo/yun/fsr"
];

// floating point data
var dht22Topics = [
    'devices/demo/dht22/temperature',
    'devices/demo/dht22/humidity'
];

client.on('connect', function () {
    console.log('Connected to MQTT');

    var success = function() {
        console.log(util.format("Registered presence %s", topics[i]));
    };

    for(var i = 0; i < topics.length; i++) {
        client.publish('presence', topics[i], {}, success);
    }

    for(i = 0; i < dht22Topics.length; i++) {
        client.publish('presence', dht22Topics[i], {}, success);
    }

    client.publish('presence', 'devices/demo/button', {}, success);

});

// this should really wait until connect success and presence is registered

// Integer Data
setInterval(function() {
    var data;
    var options = {
        qos: 0,
        retain: true
    };
    var callback = function() {
        console.log(util.format("Sent %s to %s", data, topics[i]));
    };

    for(var i = 0; i < topics.length; i++) {
        data = Math.floor(Math.random() * 255);
        client.publish(topics[i], data.toString(), options, callback);
    }

}, interval);

// DHT22 data
setInterval(function() {
    var data;
    var options = {
        qos: 0,
        retain: true
    };

    var callback = function() {
        console.log(util.format("Sent %s to %s", data, dht22Topics[i]));
    };

    for(var i = 0; i < dht22Topics.length; i++) {
        data = ((60 - 40 * i) + Math.random() * 8).toFixed(1);
        client.publish(dht22Topics[i], data.toString(), options, callback);
    }
}, dht22Interval);

// Button data is 0 or 1
setInterval(function() {
    var data = Math.round(Math.random());
    var topic = 'devices/demo/button';
    var options = {};

    var callback = function() {
        console.log(util.format("Sent %s to %s", data, topic));
    };

    client.publish(topic, data.toString(), options, callback);

}, interval);
