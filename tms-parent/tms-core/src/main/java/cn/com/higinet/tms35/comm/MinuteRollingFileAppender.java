package cn.com.higinet.tms35.comm;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;
import org.quartz.CronTrigger;
import org.quartz.TriggerUtils;

public class MinuteRollingFileAppender extends FileAppender {

    public MinuteRollingFileAppender() {
        datePattern = "'.'yyyy-MM-dd";
        nextCheck = System.currentTimeMillis() - 1L;
        now = new Date();
        rc = new RollingCalendar();
        checkPeriod = -1;
    }

    public MinuteRollingFileAppender(Layout layout, String filename, String datePattern) throws IOException {
        super(layout, filename, true);
        this.datePattern = "'.'yyyy-MM-dd";
        nextCheck = System.currentTimeMillis() - 1L;
        now = new Date();
        rc = new RollingCalendar();
        checkPeriod = -1;
        this.datePattern = datePattern;
        activateOptions();
    }

    public void setDatePattern(String pattern) {
        datePattern = pattern;
    }

    public String getDatePattern() {
        return datePattern;
    }

    public void setDateCycle(int cycle) {
        dateCycle = cycle;
    }

    public int getDateCycle() {
        return dateCycle;
    }
    
    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public void activateOptions() {
        super.activateOptions();
        if (datePattern != null && super.fileName != null) {
            now.setTime(System.currentTimeMillis());
            sdf = new SimpleDateFormat(datePattern);
            int type = computeCheckPeriod();
            printPeriodicity(type);
            rc.setType(type);
            File file = new File(super.fileName);
            if (this.cronExpression != null) {
                long lastTime = lastExecuteTime(this.cronExpression);
                long distTime = timeDistance(this.cronExpression);
                long fileLastModified = file.lastModified();
                if (lastTime > fileLastModified) {
                    long diffTime = lastTime - fileLastModified;
                    int mtp = (int) (diffTime / distTime);
                    if (mtp * distTime == diffTime) {//整数
                        lastTime = fileLastModified;
                    } else if (mtp * distTime < diffTime) {//四舍
                        lastTime -= ((mtp + 1) * distTime);
                    } else if (mtp * distTime > diffTime) {//五入
                        lastTime -= ((mtp - 1) * distTime);
                    }
                }
                scheduledFilename = super.fileName + sdf.format(new Date(lastTime));
            } else {
                scheduledFilename = super.fileName + sdf.format(new Date(file.lastModified()));
            }
        } else {
            LogLog.error("Either File or DatePattern options are not set for appender [" + super.name + "].");
        }
    }
    
    /**
     * 通过cron表达式获取最后一次执行时间
     * @param cron
     * @return
     */
    public static long lastExecuteTime(String cron) {
        long next = nextExecuteTime(cron);
        long distan = timeDistance(cron);
        return next - distan;
    }
    
    /**
     * 通过cron表达式获取下一次执行时间
     * @param cron
     * @return
     */
    @SuppressWarnings("unchecked")
    public static long nextExecuteTime(String cron) {
        try {
            CronTrigger cronTrigger = new CronTrigger();
            cronTrigger.setCronExpression(cron);
            List<Date> dates = TriggerUtils.computeFireTimes(cronTrigger, null, 1);
            return dates.get(0).getTime();
        } catch (ParseException e) {
            throw new IllegalStateException("通过cron表达式获取下一次执行时间异常！",e);
        }
    }
    
    @SuppressWarnings("unchecked")
    public static long timeDistance(String cron) {
        try {
            CronTrigger cronTrigger = new CronTrigger();
            cronTrigger.setCronExpression(cron);
            List<Date> dates = TriggerUtils.computeFireTimes(cronTrigger, null, 2);
            long last = dates.get(0).getTime(), next = dates.get(1).getTime();
            return next - last;
        } catch (ParseException e) {
            throw new IllegalStateException("cron expression analyse fail.");
        }
    }

    void printPeriodicity(int type) {
        switch (type) {
            case 0: // '\0'
                LogLog.debug("Appender [" + super.name + "] to be rolled every minute.");
                break;
            case 1: // '\001'
                LogLog.debug("Appender [" + super.name + "] to be rolled on top of every hour.");
                break;
            case 2: // '\002'
                LogLog.debug("Appender [" + super.name + "] to be rolled at midday and midnight.");
                break;
            case 3: // '\003'
                LogLog.debug("Appender [" + super.name + "] to be rolled at midnight.");
                break;
            case 4: // '\004'
                LogLog.debug("Appender [" + super.name + "] to be rolled at start of week.");
                break;
            case 5: // '\005'
                LogLog.debug("Appender [" + super.name + "] to be rolled at start of every month.");
                break;
            default:
                LogLog.warn("Unknown periodicity for appender [" + super.name + "].");
                break;
        }
    }

    int computeCheckPeriod() {
        RollingCalendar rollingCalendar = new RollingCalendar(gmtTimeZone, Locale.ENGLISH);
        Date epoch = new Date(0L);
        if (datePattern != null) {
            for (int i = 0; i <= 5; i++) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(datePattern);
                simpleDateFormat.setTimeZone(gmtTimeZone);
                String r0 = simpleDateFormat.format(epoch);
                rollingCalendar.setType(i);
                Date next = new Date(rollingCalendar.getNextCheckMillis(epoch));
                String r1 = simpleDateFormat.format(next);
                if (r0 != null && r1 != null && !r0.equals(r1))
                    return i;
            }
        }
        return -1;
    }
    
    int calendarField(int type) {
        int field = -1;
        switch (type) {
            case 0:
                field = Calendar.MINUTE;
                break;
            case 1:
                field = Calendar.HOUR_OF_DAY;
                break;
            case 2:
                field = Calendar.HOUR;
                break;
            case 3:
                field = Calendar.MONDAY;
                break;
            case 4:
                field = Calendar.DAY_OF_WEEK;
                break;
            case 5:
                field = Calendar.MONTH;
                break;
            default:
                field = Calendar.DAY_OF_MONTH;
                break;
        }
        return field;
    }

    void rollOver() throws IOException {
        if (datePattern == null) {
            super.errorHandler.error("Missing DatePattern option in rollOver().");
            return;
        }
        String datedFilename = super.fileName + sdf.format(now);
        if (scheduledFilename.equals(datedFilename))
            return;
        closeFile();
        File target = new File(scheduledFilename);
        // modify start------
        /*Calendar rollTime = Calendar.getInstance();
        rollTime.add(calendarField(rc.type), -1);
        String logFilename = fileName + sdf.format(rollTime.getTime());
        File target = new File(logFilename);*/
        // modify end------

        if (target.exists())
            target.delete();
        File file = new File(super.fileName);
        boolean result = file.renameTo(target);
        if (result)
            LogLog.debug(super.fileName + " -> " + scheduledFilename);
        else
            LogLog.error("Failed to rename [" + super.fileName + "] to [" + scheduledFilename + "].");
        try {
            setFile(super.fileName, false, super.bufferedIO, super.bufferSize);
        } catch (IOException e) {
            super.errorHandler.error("setFile(" + super.fileName + ", false) call failed.");
        }
        scheduledFilename = datedFilename;
    }

    protected void subAppend(LoggingEvent event) {
        long n = System.currentTimeMillis();
        if (n >= nextCheck) {
            now.setTime(n);
            if (cronExpression != null && cronExpression.length() > 0) {
                nextCheck = rc.getNextCheckMillis(now, cronExpression);
            } else if (dateCycle > 0) {
                nextCheck = rc.getNextCheckMillis(now, dateCycle);
            } else {
                nextCheck = rc.getNextCheckMillis(now);
            }
            try {
                rollOver();
            } catch (IOException ioe) {
                LogLog.error("rollOver() failed.", ioe);
            }            
        }
        super.subAppend(event);
    }

    static final int TOP_OF_TROUBLE = -1;
    static final int TOP_OF_MINUTE = 0;
    static final int TOP_OF_HOUR = 1;
    static final int HALF_DAY = 2;
    static final int TOP_OF_DAY = 3;
    static final int TOP_OF_WEEK = 4;
    static final int TOP_OF_MONTH = 5;
    private String datePattern;
    private String cronExpression;
    private int dateCycle;
    private String scheduledFilename;
    private long nextCheck;
    Date now;
    SimpleDateFormat sdf;
    RollingCalendar rc;
    int checkPeriod;
    static final TimeZone gmtTimeZone = TimeZone.getTimeZone("GMT");
    
    class RollingCalendar extends GregorianCalendar {

        RollingCalendar() {
            type = -1;
        }

        RollingCalendar(TimeZone tz, Locale locale) {
            super(tz, locale);
            type = -1;
        }

        void setType(int type) {
            this.type = type;
        }

        public long getNextCheckMillis(Date now) {
            return getNextCheckDate(now, 1).getTime();
        }

        public long getNextCheckMillis(Date now, int cycle) {
            return getNextCheckDate(now, cycle).getTime();
        }
        
        public long getNextCheckMillis(Date now, String cron) {
            now.setTime(lastExecuteTime(cron));
            setTime(new Date(nextExecuteTime(cron)));
            return getTimeInMillis();
        }

        public Date getNextCheckDate(Date now, int cycle) {
            setTime(now);
            switch (type) {
                case 0: // '\0'
                    set(13, 0);
                    set(14, 0);
                    add(12, cycle);
                    break;
                case 1: // '\001'
                    set(12, 0);
                    set(13, 0);
                    set(14, 0);
                    add(11, cycle);
                    break;
                case 2: // '\002'
                    set(12, 0);
                    set(13, 0);
                    set(14, 0);
                    int hour = get(11);
                    if (hour < 12) {
                        set(11, 12);
                    } else {
                        set(11, 0);
                        add(5, cycle);
                    }
                    break;
                case 3: // '\003'
                    set(11, 0);
                    set(12, 0);
                    set(13, 0);
                    set(14, 0);
                    add(5, cycle);
                    break;
                case 4: // '\004'
                    set(7, getFirstDayOfWeek());
                    set(11, 0);
                    set(12, 0);
                    set(13, 0);
                    set(14, 0);
                    add(3, cycle);
                    break;
                case 5: // '\005'
                    set(5, 1);
                    set(11, 0);
                    set(12, 0);
                    set(13, 0);
                    set(14, 0);
                    add(2, cycle);
                    break;
                default:
                    throw new IllegalStateException("Unknown periodicity type.");
            }
            return getTime();
        }

        private static final long serialVersionUID = -3560331770601814177L;
        int type;
    }
}