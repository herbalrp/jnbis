package org.jnbis;

import org.jnbis.record.HighResolutionGrayscaleFingerprint;

/**
 * @author ericdsoto
 */
public class HighResolutionGrayscaleFingerprintReader extends RecordReader {

    NistHelper.Token token;
    HighResolutionGrayscaleFingerprint fingerprint;

    public HighResolutionGrayscaleFingerprintReader(NistHelper.Token token, HighResolutionGrayscaleFingerprint fingerprint) {
        this.token = token;
        this.fingerprint = fingerprint;
    }

    @Override
    public HighResolutionGrayscaleFingerprint read() {
        if (token.pos >= token.buffer.length) {
            throw new RuntimeException("T4::NULL pointer to T4 record");
        }

        //Assigning t4-Header values
        int length = (int) readInt(token);
        int fingerPrintNo = token.buffer[token.pos + 6];

        int dataSize = length - 18;

        if (token.pos + dataSize + 17 > token.buffer.length) {
            dataSize += token.buffer.length - token.pos - 18;
        }

        byte[] data = new byte[dataSize];
        System.arraycopy(token.buffer, token.pos + 18, data, 0, data.length + 18 - 18);

        token.pos += length;
        fingerprint.setImageDesignationCharacter(Integer.toString(fingerPrintNo));
        fingerprint.setImageData(data);

        return fingerprint;
    }
}
