package fr.gouv.vitam.tools.mailextract.lib.store.microsoft.msg;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Class to hold decoded PidTagConversationIndex
 * inspired by the libpst code
 * @author Nick Buller
 */
public class MsgConversationIndex {
    private static final int HUNDRED_NS_TO_MS = 1000;
    private static final int MINIMUM_HEADER_SIZE = 22;
    private static final int RESPONSE_LEVEL_SIZE = 5;

    private Date deliveryTime;
    private UUID guid;
    private List<MicrosoftResponseLevel> responseLevels = new ArrayList<>();

    public Date getDeliveryTime() {
        return this.deliveryTime;
    }

    public UUID getGuid() {
        return this.guid;
    }

    public List<MicrosoftResponseLevel> getResponseLevels() {
        return this.responseLevels;
    }

    @Override
    public String toString() {
        return this.guid + "@" + this.deliveryTime + " " + this.responseLevels.size() + " ResponseLevels";
    }

    public class MicrosoftResponseLevel {
        short deltaCode;
        long timeDelta;
        short random;

        public MicrosoftResponseLevel(final short deltaCode, final long timeDelta, final short random) {
            this.deltaCode = deltaCode;
            this.timeDelta = timeDelta;
            this.random = random;
        }

        public short getDeltaCode() {
            return this.deltaCode;
        }

        public long getTimeDelta() {
            return this.timeDelta;
        }

        public short getRandom() {
            return this.random;
        }

        public Date withOffset(final Date anchorDate) {
            return new Date(anchorDate.getTime() + this.timeDelta);
        }
    }

    protected MsgConversationIndex(final byte[] rawConversationIndex) {
        if (rawConversationIndex != null && rawConversationIndex.length >= MINIMUM_HEADER_SIZE) {
            this.decodeHeader(rawConversationIndex);
            if (rawConversationIndex.length >= MINIMUM_HEADER_SIZE + RESPONSE_LEVEL_SIZE) {
                this.decodeResponseLevel(rawConversationIndex);
            }
        }
    }

   private static long convertBigEndianBytesToLong(final byte[] data, final int start, final int end) {

        long offset = 0;
        for (int x = start; x < end; ++x) {
            offset = offset << 8;
            offset |= (data[x] & 0xFFL);
        }

        return offset;
    }

   /**
    * <p>
    * The difference between the Windows epoch (1601-01-01
    * 00:00:00) and the Unix epoch (1970-01-01 00:00:00) in
    * milliseconds: 11644473600000L. (Use your favorite spreadsheet
    * program to verify the correctness of this value. By the way,
    * did you notice that you can tell from the epochs which
    * operating system is the modern one? :-))
    * </p>
    */
   private static final long EPOCH_DIFF = 11644473600000L;

   private static Date filetimeToDate(final int high, final int low) {
       final long filetime = ((long) high) << 32 | (low & 0xffffffffL);
       final long ms_since_16010101 = filetime / (1000 * 10);
       final long ms_since_19700101 = ms_since_16010101 - EPOCH_DIFF;
       return new Date(ms_since_19700101);
   }
   
   private void decodeHeader(final byte[] rawConversationIndex) {
        // According to the Spec the first byte is not included, but I believe
        // the spec is incorrect!
        // int reservedheaderMarker = (int)
        // PSTObject.convertBigEndianBytesToLong(rawConversationIndex, 0, 1);

        final long deliveryTimeHigh = convertBigEndianBytesToLong(rawConversationIndex, 0, 4);
        final long deliveryTimeLow = convertBigEndianBytesToLong(rawConversationIndex, 4, 6) << 16;
        this.deliveryTime = filetimeToDate((int) deliveryTimeHigh, (int) deliveryTimeLow);

        final long guidHigh = convertBigEndianBytesToLong(rawConversationIndex, 6, 14);
        final long guidLow = convertBigEndianBytesToLong(rawConversationIndex, 14, 22);

        this.guid = new UUID(guidHigh, guidLow);
    }

    private void decodeResponseLevel(final byte[] rawConversationIndex) {
        final int responseLevelCount = (rawConversationIndex.length - MINIMUM_HEADER_SIZE) / RESPONSE_LEVEL_SIZE;
        this.responseLevels = new ArrayList<>(responseLevelCount);

        for (int responseLevelIndex = 0, position = 22; responseLevelIndex < responseLevelCount; responseLevelIndex++, position += RESPONSE_LEVEL_SIZE) {

            final long responseLevelValue = convertBigEndianBytesToLong(rawConversationIndex, position,
                position + 5);
            final short deltaCode = (short) (responseLevelValue >> 39);
            final short random = (short) (responseLevelValue & 0xFF);

            // shift by 1 byte (remove the random) and mask off the deltaCode
            long deltaTime = (responseLevelValue >> 8) & 0x7FFFFFFF;

            if (deltaCode == 0) {
                deltaTime <<= 18;
            } else {
                deltaTime <<= 23;
            }

            deltaTime /= HUNDRED_NS_TO_MS;

            this.responseLevels.add(responseLevelIndex, new MicrosoftResponseLevel(deltaCode, deltaTime, random));
        }
    }
}
