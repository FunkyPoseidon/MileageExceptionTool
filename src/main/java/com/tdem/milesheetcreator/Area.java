package com.tdem.milesheetcreator;

import java.util.Scanner;

public class Area {
    String areaName;
    String district;
    String zone;
    int milesAllowed;
    String missionaryName1;
    String missionaryName2 = null;
    String missionaryName3 = null;

    public Area(String zone, String district, String areaName, int milesAllowed, String missionaryNames) {
        this.areaName = areaName;
        this.district = district;
        this.zone = zone;
        this.milesAllowed = milesAllowed;
        String[] names = getMissionaryNames(missionaryNames);
        this.missionaryName1 = names[0];

        if(names.length > 1) {
            this.missionaryName2 = names[1];
        }
        if(names.length > 2) {
            this.missionaryName3 = names[2];
        }
    }



    public Area() {
        this.areaName = "";
        this.zone = "";
        this.milesAllowed = 0;
        this.missionaryName1 = "";
        this.missionaryName2 = "";
        this.missionaryName3 = "";
    }

    private static String[] getMissionaryNames(String s) {
        // Example input pieces:
        // "Sister Cassidy Jane Dalton - Can Drive"
        // "Elder John Q Public - Something"
        Scanner stringScanner = new Scanner(s);
        stringScanner.useDelimiter(",");
        String[] names = s.split(",");


        int index = 0;
        for(String name : names) {
            String prefix = "";
            String lower = name.toLowerCase();
            if (lower.startsWith("sister ")) prefix = "S.";
            else if (lower.startsWith("elder ")) prefix = "E.";

            // remove prefix words like Sister or Elder
            String afterPrefix = name;
            if (!prefix.isEmpty()) {
                int firstSpace = name.indexOf(' ');
                if (firstSpace >= 0) afterPrefix = name.substring(firstSpace + 1).trim();
            }

            // remove trailing dash and everything after
            int dash = afterPrefix.indexOf(" - ");
            if (dash >= 0) afterPrefix = afterPrefix.substring(0, dash).trim();

            // split remaining words; assume last word is last name
            String[] words = afterPrefix.split("\\s+");
            if (words.length == 0) {
                names[index] = name;
            }
            String lastName = words[words.length - 1];

            // if there are multiple names separated by something like "Sister A - ... Sister B - ..."
            // caller handles comma-separated entries; here just return pref + last
            if (prefix.isEmpty()) {
                // default to first initial of first word if possible
                String initial = words[0].isEmpty() ? "" : words[0].substring(0, 1) + ".";
                names[index] = initial + " " + lastName;
            }
            names[index] = prefix + " " + lastName;
            index++;
        }

        return names;
    }

    String getName() {
        return areaName;
    }

    void setName(String name) {
        this.areaName = name;
    }

    String getZone() {
        return zone;
    }

    void setZone(String zone) {
        this.zone = zone;
    }

    int getMilesAllowed() {
        return milesAllowed;
    }

    void setMilesAllowed(int milesAllowed) {
        this.milesAllowed = milesAllowed;
    }

    String getMissionaryName1() {
        return missionaryName1;
    }

    void setMissionaryName1(String missionaryName1) {
        this.missionaryName1 = missionaryName1;
    }

    String getMissionaryName2() {
        return missionaryName2;
    }

    void setMissionaryName2(String missionaryName2) {
        this.missionaryName2 = missionaryName2;
    }

    String getMissionaryName3() { return  missionaryName3; }

    void setMissionaryName3(String missionaryName3) { this.missionaryName3 = missionaryName3;}

    String getMissionaryNames() {
        StringBuilder sb = new StringBuilder();

        sb.append(this.missionaryName1);

        if(missionaryName2 != null) {
            sb.append(",").append(this.missionaryName2);
        }

        if(missionaryName3 != null) {
            sb.append(",").append(this.missionaryName3);
        }

        return sb.toString();
    }

    String getDistrict() {return district;}

    public void printArea() {
        System.out.println(zone + " " + district + " " + areaName + " " + milesAllowed + " " + missionaryName1 + " " + missionaryName2);
    }

}
