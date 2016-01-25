System.register([], function(exports_1) {
    var RestUrlUtil;
    return {
        setters:[],
        execute: function() {
            /**
             * Util to calculate the rest urls, depending on if we are running in dev mode (i.e. not deployed as a servlet),
             * or deployed within Jira
             */
            RestUrlUtil = (function () {
                function RestUrlUtil() {
                }
                RestUrlUtil.caclulateUrl = function (path) {
                    var location = window.location;
                    console.log("href " + location.href);
                    var index = location.href.indexOf("/download/resources/");
                    if (index > 0) {
                        var url = location.href.substr(0, index);
                        url = url + "/plugins/servlet/jirban/" + path;
                        return url;
                    }
                    if (location.hostname === "localhost" && location.port === "3000") {
                        //For the local debugging of the UI, which does not seem to like loading json without a .json suffix
                        return path + ".json";
                    }
                    return path;
                };
                return RestUrlUtil;
            })();
            exports_1("RestUrlUtil", RestUrlUtil);
        }
    }
});
//# sourceMappingURL=RestUrlUtil.js.map