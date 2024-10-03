export function gatherData(formElem) {
	var data = {};

	var inputs = $("input[type='text'], option:selected", formElem).each(
		function () {
			var input = $(this);
			var name = null;
			var value = null;
			if (input.prop("tagName") == "INPUT") {
				name = input.attr("name");
				value = input.val();
			} else if (input.prop("tagName") == "OPTION") {
				name = input.parents("select").attr("name");
				value = input.attr("value");
			}
			if (name != null && value != null) {
				if (data[name] == null)
					data[name] = new Array();
				data[name].push(value);
			}
		});

	return data;
}

export function sortOptionsByName(selector) {
	$(selector).html($(selector + " option").sort(function (a, b) {
		return a.text == b.text ? 0 : a.text < b.text ? -1 : 1
	}))
	$(selector)[0].selectedIndex = 0;
}

	
export function leagueErrorMap(outputControl, callback=null) {
	return {
		"0":function() {
			outputControl.html("0: Server has been shut down or there was a problem with your internet connection.", "warningMessage");
			if(callback!=null)
				callback();
		},
		"400":function() {
			outputControl.html("400: One of the provided parameters was malformed.  Double-check your input and try again.");
			if(callback!=null)
				callback();
		},
		"401":function() {
			outputControl.html("401: You are not logged in.");
			if(callback!=null)
				callback();
		},
		"403": function() {
			outputControl.html("403: You do not have permission to perform such actions.");
			if(callback!=null)
				callback();
		},
		"404": function() {
			outputControl.html("404: Info not found.  Check that your input is correct with removed whitespace and try again.");
			if(callback!=null)
				callback();
		},
		"410": function() {
			outputControl.html("410: You have been inactive for too long and were loggedout. Refresh the page if you wish to re-stablish connection.");
			if(callback!=null)
				callback();
		},
		"500": function() {
			outputControl.html("500: Server error. One of the provided parameters was probably malformed.  Double-check your input and try again.");
			if(callback!=null)
				callback();
		}
    }
}