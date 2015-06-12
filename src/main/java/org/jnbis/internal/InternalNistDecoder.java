package org.jnbis.internal;

import org.jnbis.Bitmap;
import org.jnbis.DecodedData;
import org.jnbis.ImageUtils;
import org.jnbis.internal.record.BaseRecord;
import org.jnbis.internal.record.reader.factory.RecordReaderFactory;
import org.jnbis.record.*;


/**
 * @author hamed
 * @version 1.0
 * @since Apr 29, 2007
 */
public class InternalNistDecoder {
    private InternalWsqDecoder wsqDecoder;
    private ImageUtils imageUtils;
    private RecordReaderFactory readerFactory;

    public InternalNistDecoder() {
        wsqDecoder = new InternalWsqDecoder();
        imageUtils = new ImageUtils();
        readerFactory = new RecordReaderFactory();
    }

    public DecodedData decode(byte[] nist, DecodedData.Format fingerImageFormat) {
        if (nist == null || nist.length == 0) {
            throw new IllegalArgumentException("data is null or zero length");
        }

        NistHelper.Token token = new NistHelper.Token(nist);
        InternalDecodedData decoded = new InternalDecodedData();
        BaseRecord record = readerFactory.read(token);
        decoded.putTransactionInfo(decoded.getTransactionKeys().size(), (TransactionInformation) record);

        while (nextRecord(token)) {
            if (token.crt < 2) {
                continue;
            }
            record = readerFactory.read(token);

            if (record instanceof UserDefinedDescriptiveText) {
                decoded.putUserDefinedText(decoded.getUserDefinedTextKeys().size(), (UserDefinedDescriptiveText) record);

            } else if (record instanceof LowResolutionGrayscaleFingerprint) {
                LowResolutionGrayscaleFingerprint fingerprint = (LowResolutionGrayscaleFingerprint) record;
                decoded.putLowResGrayscaleFingerPrint(decoded.getLowResGrayscaleFingerPrintKeys().size(), fingerprint);

            } else if (record instanceof HighResolutionGrayscaleFingerprint) {
                HighResolutionGrayscaleFingerprint fingerprint = (HighResolutionGrayscaleFingerprint) record;
                if (WsqHelper.isWsqFormat(fingerprint.getImageData())) {
                    Bitmap bitmap = wsqDecoder.decode(fingerprint.getImageData());

                    switch (fingerImageFormat) {
                        case JPEG:
                            fingerprint.setImageData(imageUtils.bitmap2jpeg(bitmap));
                            break;
                        case GIF:
                            fingerprint.setImageData(imageUtils.bitmap2gif(bitmap));
                            break;
                        case PNG:
                            fingerprint.setImageData(imageUtils.bitmap2png(bitmap));
                            break;
                        default:
                            throw new RuntimeException("unsupported image format.");
                    }
                }
                decoded.putHiResGrayscaleFingerPrint(decoded.getHiResGrayscaleFingerPrintKeys().size(), fingerprint);

            } else if (record instanceof LowResolutionBinaryFingerprint) {
                LowResolutionBinaryFingerprint fingerprint = (LowResolutionBinaryFingerprint) record;
                decoded.putLowResBinaryFingerPrint(decoded.getLowResBinaryFingerPrintKeys().size(), fingerprint);

            } else if (record instanceof HighResolutionBinaryFingerprint) {
                HighResolutionBinaryFingerprint fingerprint = (HighResolutionBinaryFingerprint) record;
                decoded.putHiResBinaryFingerPrint(decoded.getHiResBinaryFingerPrintKeys().size(), fingerprint);

            } else if (record instanceof UserDefinedImage) {
                decoded.putUserDefinedImage(decoded.getUserDefinedImageKeys().size(), (UserDefinedImage) record);

            } else if (record instanceof SignatureImage) {
                decoded.putSignature(decoded.getSignatureKeys().size(), (SignatureImage) record);

            } else if (record instanceof MinutiaeData) {
                //readMinutiaeData(token, decoded);
                decoded.putMinutiaeData(decoded.getMinutiaeDataKeys().size(), (MinutiaeData) record);

            } else if (record instanceof FacialAndSmtImage) {
                decoded.putFacialSmtImage(decoded.getFacialSmtKeys().size(), (FacialAndSmtImage) record);

            } else if (record instanceof VariableResolutionLatentImage) {
                decoded.putVariableResLatentImage(decoded.getVariableResLatentImageKeys().size(), (VariableResolutionLatentImage) record);

            } else if (record instanceof VariableResolutionFingerprint) {
                decoded.putVariableResFingerprint(decoded.getVariableResFingerprintKeys().size(), (VariableResolutionFingerprint) record);

            } else if (record instanceof VariableResolutionPalmprint) {
                decoded.putVariableResPalmprint(decoded.getVariableResPalmprintKeys().size(), (VariableResolutionPalmprint) record);

            } else if (record instanceof IrisImage) {
                decoded.putIrisImage(decoded.getIrisImageKeys().size(), (IrisImage) record);
            }
        }

        return decoded;
    }

    private boolean nextRecord(NistHelper.Token token) {
        if (token.header.length() == 0) {
            return false;
        }

        int rsPos = token.header.indexOf(NistHelper.SEP_RS);
        if (rsPos == -1) {
            rsPos = token.header.length() - 1;
        }

        int usPos = token.header.indexOf(NistHelper.SEP_US);
        token.crt = Integer.parseInt(token.header.substring(0, usPos));
        token.header = token.header.substring(rsPos + 1);

        return true;
    }

}