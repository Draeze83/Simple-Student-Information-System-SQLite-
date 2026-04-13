package com.sis.model;

public class Program {
    private int    id;
    private String code;
    private String name;
    private String collegeCode;
    private String collegeName;   // joined field for display

    public Program() {}
    public Program(int id, String code, String name, String collegeCode, String collegeName) {
        this.id = id; this.code = code; this.name = name;
        this.collegeCode = collegeCode; this.collegeName = collegeName;
    }

    public int    getId()          { return id; }
    public void   setId(int id)    { this.id = id; }
    public String getCode()        { return code; }
    public void   setCode(String c){ this.code = c; }
    public String getName()        { return name; }
    public void   setName(String n){ this.name = n; }
    public String getCollegeCode() { return collegeCode; }
    public void   setCollegeCode(String cc){ this.collegeCode = cc; }
    public String getCollegeName() { return collegeName; }
    public void   setCollegeName(String cn){ this.collegeName = cn; }

    /** Used in combo-boxes so the user sees a meaningful label. */
    @Override public String toString() { return code + " – " + name; }
}
