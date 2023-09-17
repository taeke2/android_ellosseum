package com.gbsoft.ellosseum.dto;

public class SubcontractorDTO {
    private int mId;
    private String mName;
    private String mTel;
    private String mWorkType;
    private String mManagerName;
    private String mRemark;

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getTel() {
        return mTel;
    }

    public void setTel(String tel) {
        mTel = tel;
    }

    public String getWorkType() {
        return mWorkType;
    }

    public void setWorkType(String workType) {
        mWorkType = workType;
    }

    public String getManagerName() {
        return mManagerName;
    }

    public void setManagerName(String managerName) {
        mManagerName = managerName;
    }

    public String getRemark() {
        return mRemark;
    }

    public void setRemark(String remark) {
        mRemark = remark;
    }
}
