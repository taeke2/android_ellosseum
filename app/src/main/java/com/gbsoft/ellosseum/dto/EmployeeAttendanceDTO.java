package com.gbsoft.ellosseum.dto;

public class EmployeeAttendanceDTO {
    private int mId;
    private String date;
    private String mAttendanceDateTime;
    private String mLeaveWorkDateTime;

    public EmployeeAttendanceDTO(int id, String date, String attendanceDateTime, String leaveWorkDateTime) {
        mId = id;
        this.date = date;
        mAttendanceDateTime = attendanceDateTime;
        mLeaveWorkDateTime = leaveWorkDateTime;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAttendanceDateTime() {
        return mAttendanceDateTime;
    }

    public void setAttendanceDateTime(String attendanceDateTime) {
        mAttendanceDateTime = attendanceDateTime;
    }

    public String getLeaveWorkDateTime() {
        return mLeaveWorkDateTime;
    }

    public void setLeaveWorkDateTime(String leaveWorkDateTime) {
        mLeaveWorkDateTime = leaveWorkDateTime;
    }
}
