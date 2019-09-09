var viveSourcecodeController = (function(){

    var sourcecode = ""
    var sectionSize = 400
    var beginSection = 0
    var endSection = 400

    /**  
     * actual section shown in the sourcecode ui element
     */
    var sectionPart = 1
    
    /**
     * number of sections in the source code given by the sectionSize
     */
    var sectionsCount = 0

    function init(code) {
        sourcecode = code
        beginSection = 0
        endSection = 400
        sectionPart = 1
        sectionsCount = (sourcecode.length / sectionSize).toFixed(0)
    }

    /**
     * makes the sourcecode element in the UI visible
     * 
     * @param {String} code The Sourcecode as String which should be shown
     */
    function showSourcecode(code) {
        if (sourcecode !== code) {
            init(code)
        }
        document.getElementById('vive-sourcecode').innerHTML = 
            `<a-text position="0 0 -7" value="${sourcecode.substring(beginSection, endSection)}"></a-text>`
        document.getElementById('vive-sourcecode').object3D.visible = true
    }

    /**
     * hides the sourcecode element
     */
    function hideSourcecode() {
        document.getElementById('vive-sourcecode').object3D.visible = false
    }

    /**
     * shows the next section of the sourceCode
     */
    function scrollCode() {
        if (document.getElementById('vive-sourcecode').object3D.visible === false) {
            return;
        }
        var length = sourcecode.length

        beginSection = beginSection + sectionSize
        sectionPart = sectionPart + 1

        if (beginSection > length) {
            sectionPart = 1
            beginSection = 0
            endSection = sectionSize
            showSourcecode(sourcecode)
            return;
        }
        endSection = endSection + sectionSize
        if (endSection > length) {
            endSection = length
        }
        showSourcecode(sourcecode)
    }
    
    return {
        showSourcecode: showSourcecode,
        hideSourcecode: hideSourcecode,
        scrollCode: scrollCode
    }
})()