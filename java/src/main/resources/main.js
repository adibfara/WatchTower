//this file needs heavy refactoring!

calls = []
HTMLElement = typeof(HTMLElement) != 'undefined' ? HTMLElement : Element;

HTMLElement.prototype.prepend = function(element) {
    if (this.firstChild) {
        return this.insertBefore(element, this.firstChild);
    } else {
        return this.appendChild(element);
    }
};
last_id = 0

function addRequest(request){
 testData = '{ '+
            '    "requestData": {' +
            '    "url": "http://google.com/salam",' +
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
            '  "responseCode": 200,' +
            '  "body": {"body" : "{\\"salam\\":true}"},' +
            '  "contentLength": 3700'+
            '}'
 last_id += 1
 console.log(testData)
 response = JSON.parse(testData)
 response['callId'] = last_id
 calls.push(response)
 response_body = (response.body != null && response.body.body!=null)? JSON.stringify(JSON.parse(response.body.body), null, 2) : "NO RESPONSE BODY"
 request_body = (response.requestData.body != null && response.requestData.body.body!=null)? JSON.stringify(JSON.parse(response.requestData.body.body), null, 2) : "NO REQUEST BODY"
// document.getElementById("response_body").prepend(getJsonHtml(response_body))
// document.getElementById("request_body").prepend(getJsonHtml(request_body))
var container = document.getElementById("requests_container").prepend(getRequestHTML(response))
}
function getJsonHtml(string){
return htmlToElement(
        '<pre><code class="json">'+string+'</code></pre>'
        )
}

function htmlToElement(html) {
    var template = document.createElement('template');
    html = html.trim(); // Never return a text node of whitespace as the result
    template.innerHTML = html;
    return template.content.firstChild;
}
function getRequestHTML(response){
request = response.requestData
id = response.callId
url = request.url
responseCode = response.responseCode
method = request.method
tookTime = response.tookTime
isSuccess = (responseCode >=200 && responseCode <300)
classname = isSuccess ? 'success' : 'failure'
success = isSuccess ? "SUCCESS" : "FAILED"
contentLength = response.contentLength /1000
return htmlToElement('<div class="animatable request card '+classname+'" on id="'+id+'">' +
                       '                 <div class="url">' +
                       '                     <h3>'+method+'</h3>' +
                       '                     <h2>'+url+'</h2>' +
                       '                 </div>' +
                       '                 <div class="meta">' +
                       '                     <div class="response">' +
                       '                       <span class="length">' +
                       '                           '+success+' '+responseCode+'' +
                       '                         </span>' +
                       '                     </div>' +
                       '                     <div class="info">' +
                       '                         <span class="length">' +
                       '                             <strong>'+contentLength+'</strong> Kilobytes' +
                       '                         </span>' +
                       '                         <span class="time">' +
                       '                             <strong>'+tookTime+'</strong>ms' +
                       '                         </span>' +
                       '                     </div>' +
                       '                 </div>' +
                       '             </div>')
}
addRequest("khiar")
setInterval(function() {addRequest("khiar")}, 5000)

function setupClickHandlers(){
$(".request").click(function(e){
  alert(e.target.id)
});
}
setupClickHandlers()







/**
TABS
*/
function openCity(evt, cityName) {
  // Declare all variables
  var i, tabcontent, tablinks;

  // Get all elements with class="tabcontent" and hide them
  tabcontent = document.getElementsByClassName("tabcontent");
  for (i = 0; i < tabcontent.length; i++) {
    tabcontent[i].style.display = "none";
  }

  // Get all elements with class="tablinks" and remove the class "active"
  tablinks = document.getElementsByClassName("tablinks");
  for (i = 0; i < tablinks.length; i++) {
    tablinks[i].className = tablinks[i].className.replace(" active", "");
  }

  // Show the current tab, and add an "active" class to the button that opened the tab
  document.getElementById(cityName).style.display = "block";
  evt.currentTarget.className += " active";
}