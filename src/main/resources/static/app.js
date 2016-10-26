var stompClient;

function start(fileInput) {
    var files = $(fileInput).prop('files');
    if (!files || files.length != 1) {
        return false;
    }
    var file = files[0];

    $('#progressbar').progressbar('option', 'max', file.size);

    connect(function() {
        $.ajax('/create', {
            type: 'POST',
            dataType: 'json',
            contentType: 'application/json',
            data: JSON.stringify({name: file.name, size: file.size})
        }).done(function(id) {
            stompClient.subscribe('/topic/data/' + id + "/done", function (message) {
                if (JSON.parse(message.body)) {
                    window.location = '/' + id + '.htm';
                }
                else {
                    alert('Parse failed');
                }
            });

            stompClient.subscribe('/topic/data/' + id + "/request", function (message) {
                var request = JSON.parse(message.body);
                var offset = parseInt(request.offset, 16);
                var size = request.size;

                var reader = new FileReader();
                reader.onload = function(e) {
                    var buffer = e.target.result.split(',')[1];
                    var data = {
                        offset: request.offset,
                        size: request.size,
                        buffer: buffer
                    }
                    $('#progressbar').progressbar( "option", "value", offset);
                    $('#status').text('Processing data request: offset ' + offset + ', size ' + size);
                    stompClient.send("/metal/data/" + id + "/response", {}, JSON.stringify(data));
                };
                var slice = file.slice(offset, offset + size);
                reader.readAsDataURL(slice);
            });
            stompClient.send("/metal/data/" + id + "/start", {}, 'start');
        });
    });
}

function connect(callback) {
    if (stompClient) {
        callback();
    }
    else {
        var socket = new SockJS('/websocket');
        stompClient = Stomp.over(socket);
        stompClient.connect({}, function (frame) {
            callback();
        });
    }
}

$(function () {
    $('#progressbar').progressbar({
        value: 0
    });
});