package ca.bc.gov.open.cdds.models.serializers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class InstantSoapConverter {

    private InstantSoapConverter() {}

    public static String print(Instant xml) {
        String first = xml.toString();
        return first.substring(0, first.length() - 1);
    }

    public static Instant parse(String value) {
        try {
            Date d;
            // Try to parse a datetime first then try date only if both fail return null
            try {
                // Date time parser
                var sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSSSSS", Locale.US);
                sdf.setTimeZone(TimeZone.getTimeZone("GMT-7"));
                d = sdf.parse(value);
            } catch (ParseException ex) {
                // Date only parser
                try {
                    var sdf = new SimpleDateFormat("dd-MMM-yy", Locale.US);
                    sdf.setTimeZone(TimeZone.getTimeZone("GMT-7"));
                    d = sdf.parse(value);
                } catch (ParseException ex2) {
                    return Instant.parse(value + "Z");
                }
            }
            return d.toInstant();
        } catch (Exception ex) {
            log.warn("Bad date received from soap request");
            return null;
        }
    }

    public static String convertFromAmTo24(String appearanceTime) throws ParseException {
        var sdf = new SimpleDateFormat("dd-MMM-yy hh.mm.ss.SSSSSS a", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT-7"));
        var d = sdf.parse(appearanceTime).toInstant();
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S")
                .withZone(ZoneId.of("GMT-7"))
                .withLocale(Locale.US)
                .format(d);
    }
}
