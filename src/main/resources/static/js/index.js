$(document).ready(function () {

    showHome();

    function removeActive() {
        $("li.nav-item").removeClass('active');
    }

    $('#textInput').on('change keyup paste', function () {
        console.log();
        if($('#textInput').val().length > 1 || ($('.custom-file-input').val() !== undefined && $('.custom-file-input').val() !== '')){
            $("#submitBtn").removeAttr('disabled');
        }else{
            $("#submitBtn").attr('disabled', 'disabled');
        }
    });

    /* show file value after file select */
    $('.custom-file-input').on('change', function () {
        var filename = $(this).val();
        if (filename === undefined || filename === '') {
            errorAlert('Arquivo não selecionado.');
            return;
        }
        clearAlert();
        filename = filename.substring(filename.lastIndexOf('\\') + 1, filename.length);
        $(this).next('.custom-file-label').addClass("selected").html(filename);
        $("#submitBtn").removeAttr('disabled');
    });

    $('#fileUploadForm').submit(function (event) {
        event.preventDefault();
        doFileUpload(new FormData(this));
    });

    $('#btnGetFiles').click(function (event) {
        event.preventDefault();
        doGetFiles();
    });

    $("#homeLink").click(function (event) {
        removeActive();
        $(this).parent().addClass('active');
        showHome();
    });

    $("#helpLink").click(function (event) {
        removeActive();
        $(this).parent().addClass('active');
        $('#fileUploadForm').hide();
        $('#result').hide();
        $('#help').show();
        $('#config').hide();
    });

    $("#configLink").click(function (event) {
        removeActive();
        $(this).parent().addClass('active');
        $('#fileUploadForm').hide();
        $('#result').hide();
        $('#help').hide();
        $('#config').show();
    });

    function showHome() {
        $('#fileUploadForm').show();
        $('#result').show();
        $('#help').hide();
        $('#config').hide();
    }

    function doFileUpload(data) {
        if (window.FormData === undefined) {
            console.log('Not Supported.');
            return;
        }
        $('#textInput').empty();
        console.log(data);

        $.ajax({
            type: 'POST',
            enctype: 'multipart/form-data',
            url: '/api/uploadfile',
            data: data,
            processData: false, //prevent jQuery from automatically transforming the data into a query string
            contentType: false,
            cache: false,
            success: function (data) {
                clearAlert();
                doCalculate();
            },
            error: function (e) {
                errorAlert(text);
            }
        });
    }

    function doCalculate() {
        console.log($('#steps, #textInput').serialize());

        $.ajax({
            url: '/convert',
            data: $('#steps, #textInput').serialize() ,
            method: 'GET',
            success: function (data) {
                console.log(data);
                printResult(data);
            },
            error: function (e) {
                console.log(e);
                errorAlert(e.responseText);
            },
            dataType: 'json'
        });
    }

    function clearAlert() {
        $("#alerts").removeAttr('class');
        $("#alerts").text('');
    }

    function errorAlert(text) {
        $("#alerts").addClass('alert alert-block alert-danger');
        $("#alerts").text(text);
    }

    function printResult(data) {

        var chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
        var step = 0;

        var text = '<div class="row gray">';
        text += '<div class="col-md-6 text-center">Dimensão da matriz: <b>' + data.dimension + '</b></div>';
        text += '<div class="col-md-6 text-center">Maior número absoluto: <b>' + data.max + '</b></div>';
        text += '</div>';


        text += '<div class="d-flex flex-row flex-wrap">';
        text += buildMatrix('Matriz inserida', data.matrixList[step++]);
        text += buildMatrix('Matriz Identidade', data.matrixList[step++]);
        text += buildMatrix('DTMC convertida', data.matrixList[step++]);
        for (; step < data.matrixList.length; step++) {
            text += buildMatrix('M<sup>' + (step - 1) + '</sup>', data.matrixList[step], true);
        }
        text += '</div>';//End flex

        text += '<div class="d-flex flex-row flex-wrap">';
        text += '<div class="card"><div class="card-body">';
        text += '<h5 class="card-title">Probabilidades para cada estado</h5><p class="card-text">';
        text += '<table>';
        for (var row = 0; row < data.results.length; row++) {
            text += '<tr><td>' + chars.charAt(row) + ':</td><td>' + (data.results[row] * 100).toFixed(2) + '%</td>';
        }
        text += '</table>';
        text += '</p></div></div>';

        text += '<div class="card"><div class="card-body">';
        text += '<h5 class="card-title">Exemplo prático com ' + $('#steps').val() + ' tentativas</h5><p class="card-text">';
        text += '<table>';
        for (var row = 0; row < data.results.length; row++) {
            text += '<tr><td>' + chars.charAt(row) + ':</td><td>' + data.counter[row] + '</td>';
        }
        text += '</table>';
        text += '</p></div></div>';
        text += '</div>';

        $("#result").html(text);
    }


    function buildMatrix(title, matrix, round = false) {

        var text = '<div class="card">';
        text += '<div class="card-body">';
        text += '<h6 class="card-title">' + title + '</h6>';
        text += '<p class="card-text">';
        text += '<table class="matrix">';
        for (var row = 0; row < matrix.length; row++) {
            text += '<tr>';
            for (var col = 0; col < matrix[row].length; col++) {
                if (col > 0) {
                    text += '<td>&nbsp;</td>';
                }
                text += '<td>' + format(matrix[row][col], 4) + '</td>';
            }
            text += '</tr>';
        }
        text += '</table>';
        text += '</p>'
        text += '</div>'
        text += '</div>';
        return text;
    }

    function format(val, digits) {
        var str = '' + val.toFixed(digits);
        var idx = str.length;
        while (--idx > 0) {
            if (str[idx] !== '0') {
                if (str[idx] !== '.') {
                    idx++;
                }
                break;
            }
        }
        return str.substring(0, idx);
    }

});