package com.sis.model;

public class Student {
    private String id;           // YYYY-NNNN
    private String firstname;
    private String lastname;
    private int    programId;
    private String programCode;  // joined
    private String programName;  // joined
    private String collegeCode;  // joined
    private String collegeName;  // joined
    private int    year;         // year level 1-5
    private String gender;

    public Student() {}

    public String getId()           { return id; }
    public void   setId(String v)   { this.id = v; }
    public String getFirstname()    { return firstname; }
    public void   setFirstname(String v){ this.firstname = v; }
    public String getLastname()     { return lastname; }
    public void   setLastname(String v) { this.lastname = v; }
    public int    getProgramId()    { return programId; }
    public void   setProgramId(int v)   { this.programId = v; }
    public String getProgramCode()  { return programCode; }
    public void   setProgramCode(String v){ this.programCode = v; }
    public String getProgramName()  { return programName; }
    public void   setProgramName(String v){ this.programName = v; }
    public String getCollegeCode()  { return collegeCode; }
    public void   setCollegeCode(String v){ this.collegeCode = v; }
    public String getCollegeName()  { return collegeName; }
    public void   setCollegeName(String v){ this.collegeName = v; }
    public int    getYear()         { return year; }
    public void   setYear(int v)    { this.year = v; }
    public String getGender()       { return gender; }
    public void   setGender(String v)   { this.gender = v; }

    public String getFullName() { return lastname + ", " + firstname; }
}
