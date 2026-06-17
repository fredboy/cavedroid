package ru.fredboy.cavedroid.html

import org.teavm.jso.JSBody
import org.teavm.jso.JSFunctor
import org.teavm.jso.JSObject
import ru.fredboy.cavedroid.common.api.SaveTransferController
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@JSFunctor
private fun interface ImportCallback : JSObject {
    fun onResult(base64: String?)
}

// Decode base64 -> Uint8Array -> Blob, then click a temporary download anchor.
@JSBody(
    params = ["base64", "fileName"],
    script = """
        var binary = atob(base64);
        var len = binary.length;
        var bytes = new Uint8Array(len);
        for (var i = 0; i < len; i++) {
            bytes[i] = binary.charCodeAt(i);
        }
        var blob = new Blob([bytes], { type: 'application/zip' });
        var url = URL.createObjectURL(blob);
        var a = document.createElement('a');
        a.href = url;
        a.download = fileName;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);
    """,
)
private external fun downloadBase64(base64: String, fileName: String)

// Open a file picker, read the chosen file, and hand back its base64 contents.
// The byte->string conversion is chunked to avoid call-stack limits / O(n^2)
// string growth on multi-MB saves.
@JSBody(
    params = ["callback"],
    script = """
        var input = document.createElement('input');
        input.type = 'file';
        input.accept = '.cvdworld';
        input.onchange = function() {
            var file = input.files && input.files[0];
            if (!file) { callback(null); return; }
            var reader = new FileReader();
            reader.onload = function() {
                var bytes = new Uint8Array(reader.result);
                var binary = '';
                var chunk = 0x8000;
                for (var i = 0; i < bytes.length; i += chunk) {
                    binary += String.fromCharCode.apply(
                        null, bytes.subarray(i, Math.min(i + chunk, bytes.length))
                    );
                }
                callback(btoa(binary));
            };
            reader.onerror = function() { callback(null); };
            reader.readAsArrayBuffer(file);
        };
        input.click();
    """,
)
private external fun pickFileBase64(callback: ImportCallback)

class WebSaveTransferController : SaveTransferController {

    override val isSupported: Boolean = true

    @OptIn(ExperimentalEncodingApi::class)
    override fun exportSave(
        suggestedFileName: String,
        zipBytes: ByteArray,
        onResult: (success: Boolean) -> Unit,
    ) {
        downloadBase64(Base64.encode(zipBytes), suggestedFileName)
        onResult(true)
    }

    @OptIn(ExperimentalEncodingApi::class)
    override fun importSave(onResult: (zipBytes: ByteArray?) -> Unit) {
        pickFileBase64 { base64 ->
            onResult(base64?.let { Base64.decode(it) })
        }
    }
}
