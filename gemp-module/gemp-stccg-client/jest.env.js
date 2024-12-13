// JQuery and JQueryUI setup
global.window = window;
global.$ = require('./src/main/web/js/jquery/jquery-3.7.1');
global.jQuery = global.$;
require('./src/main/web/js/jquery/jquery-ui-1.14.1/jquery-ui');

// adds the 'fetchMock' global variable and rewires 'fetch' global to call 'fetchMock' instead of the real implementation
require('jest-fetch-mock').enableMocks()