//this file needs heavy refactoring!

calls = [];
HTMLElement = typeof (HTMLElement) != 'undefined' ? HTMLElement : Element;

// SCROLL TO BOTTOM
var out = document.getElementById("requests_container");


HTMLElement.prototype.prepend = function (element) {
    if (this.firstChild) {
        return this.insertBefore(element, this.firstChild);
    } else {
        return this.appendChild(element);
    }
};
last_id = 0;

function addRequest(request) {
    testData = '{ ' +
        '    "requestData": {' +
        '    "url": "http://google.com/salam/asdfasdf/s/fsd//dsf/sdfasfasd/fsdasdfaf/da/fdsffdsadfsd/sd/dsaf",' +
        '    "headers": [' +
        '      {' +
        '        "key": "khiar",' +
        '        "value": "wtf"' +
        '      },' +
        '      {' +
        '        "key": "khiar2",' +
        '        "value": "wtf2"' +
        '      }' +
        '    ],' +
        '    "method": "GET"' +
        '  },' +
        '  "headers": [' +
        '    {' +
        '      "key": "response header 1",' +
        '      "value": "wtasdfsadff"' +
        '    },' +
        '    {' +
        '      "key": "response header 2",' +
        '      "value": "wtdsfasdff2"' +
        '    }' +
        '  ],' +
        '  "tookTime": 47,' +
        '  "responseCode": ' + (Math.random() >= 0.5 ? 500 : 200) + ',' +
        '  "body": {"body" : "{\\"salam\\":true}"},' +
        '  "contentLength": 3700' +
        '}';
    processRequest(JSON.parse(testData));
}

function processRequest(call) {
    if (call.requestData != null) {
        last_id += 1;
        call['callId'] = last_id;
        calls.push(call);
        document.getElementById("requests_container").append(getRequestHTML(call));
    }

}

function getJsonHtml(string) {
    return htmlToElement(
        '<pre><code class="json">' + string + '</code></pre>'
    )
}

function htmlToElement(html) {
    var template = document.createElement('template');
    html = html.trim(); // Never return a text node of whitespace as the result
    template.innerHTML = html;
    return template.content.firstChild;
}

function getRequestHTML(response) {
    request = response.requestData;
    id = response.callId;
    url = request.url;
    responseCode = response.responseCode;
    method = request.method;
    tookTime = response.tookTime;
    isSuccess = (responseCode >= 200 && responseCode < 300);
    classname = isSuccess ? 'success' : 'failure';
    success = isSuccess ? "SUCCESS" : "FAILED";
    contentLength = response.contentLength / 1000;
    return htmlToElement('<div class="animatable request card ' + classname + '" id="' + id + '">' +
        '                 <div class="url">' +
        '                     <h3>' + method + '</h3>' +
        '                     <h2>' + url + '</h2>' +
        '                 </div>' +
        '                 <div class="meta">' +
        '                     <div class="response">' +
        '                       <span class="length">' +
        '                           ' + success + ' ' + responseCode + '' +
        '                         </span>' +
        '                     </div>' +
        '                     <div class="date-container">' +
        '                         <span class="date">' +
        '                             ' + getCurrentTime() + '</>' +
        '                         </span>' + '</div>' +
        '                     <div class="info">' +
        '                         <span class="length">' +
        '                             <strong>' + contentLength + '</strong> Kilobytes' +
        '                         </span>' +
        '                         <span class="time">' +
        '                             <strong>' + tookTime + '</strong>ms' +
        '                         </span>' +
        '                     </div>' +
        '                 </div>' +
        '             </div>')
}

/*
only for testing purposes
addRequest("khiar");
setInterval(function () {
    addRequest("khiar")
}, 1000);
*/

function setupClickHandlers() {
    $(document).on('click', '.request', function () {
        $(".request").removeClass("active");
        $(this).addClass("active");
        var clickedId = (this.id);
        console.log(clickedId);
        $("#request_headers").html("");
        $("#response_headers").html("");
        $("#request_data").html("");
        $("#response_data").html("");
        $(".data-container ").show();
        call = calls.find(function (value) {
            return parseInt(value['callId']) === parseInt(clickedId)
        });
        console.log(call);
        call.requestData.headers.forEach(function (value) {
            $("#request_headers").append("<p> <span class='header_key'>" + value['key'] + " :</span><span class='header_value'>" + value['value'] + "</span></p>")
        });
        call.headers.forEach(function (value) {
            $("#response_headers").append("<p> <span class='header_key'>" + value['key'] + " :</span><span class='header_value'>" + value['value'] + "</span></p>")
        });
        request_body = (call.requestData.body != null && call.requestData.body.body != null) ?
            "<pre><code class=\"json\">" + JSON.stringify(JSON.parse(call.requestData.body.body), null, 2) + "</code></pre>"
            : "NO REQUEST BODY";
        response_body = (call.body != null && call.body.body != null) ?
            "<pre><code class=\"json\">" + JSON.stringify(JSON.parse(call.body.body), null, 2) + "</code></pre>"
            : "NO RESPONSE BODY";
        $("#request_data").html(request_body);
        $("#response_data").html(response_body);
        hljs.highlightBlock(document.querySelector('code'))
    });


}

setupClickHandlers();


$('.tabgroup > div').hide();
$('.tabgroup > div:first-of-type').show();
$('.tabs a').click(function (e) {
    e.preventDefault();
    var $this = $(this),
        tabgroup = '#' + $this.parents('.tabs').data('tabgroup'),
        others = $this.closest('li').siblings().children('a'),
        target = $this.attr('href');
    others.removeClass('active');
    $this.addClass('active');
    $(tabgroup).children('div').hide();
    $(target).show();

});


function getCurrentTime() {
    var today = new Date();
    var date = today.getFullYear() + '-' + (today.getMonth() + 1) + '-' + today.getDate();
    var time = today.getHours() + ":" + today.getMinutes() + ":" + today.getSeconds();
    return date + ' ' + time;

}


/**
 WEBSOCKET
 **/

function connectToWebsocket() {

    if ("WebSocket" in window) {

        var wesocketAddress = "ws://" + location.hostname + ":5003/";
        console.log(wesocketAddress);
        // Let us open a web socket
        var ws = new WebSocket(wesocketAddress);

        ws.onopen = function () {

            // Web Socket is connected, send data using send()
            ws.send('{"connected": true}');
            console.log("Message is sent...");
        };

        ws.onmessage = function (evt) {
            var data = evt.data;
            var received_msg = JSON.parse(data);
            var type = received_msg.type;
            if (type === 'RESPONSE') {
                processRequest(received_msg.data);
            } else if (type === 'BATCH_RESPONSE') {
                var allResponses = received_msg.data;
                allResponses.forEach(function (response) {
                    processRequest(response);
                })
            }
            console.log("Message is received...");

        };

        ws.onclose = function () {

            // websocket is closed.
            console.log("Connection is closed...");
        };
    } else {

        // The browser doesn't support WebSocket
        alert("WebSocket NOT supported by your Browser!");
    }
}

connectToWebsocket();

$("#clear-button").click(function () {
    $("#requests_container").html("");
    $(".data-container").hide()
});
