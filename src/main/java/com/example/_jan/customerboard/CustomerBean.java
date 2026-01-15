package com.example._jan.customerboard;

public class CustomerBean {

        String mobile, name, area, hawker, paper;

        public CustomerBean(String mobile, String name, String area, String hawker, String paper) {
            this.mobile = mobile;
            this.name = name;
            this.area = area;
            this.hawker = hawker;
            this.paper = paper;
        }

        // Getters (Zaroori hain)
        public String getMobile() { return mobile; }
        public String getName() { return name; }
        public String getArea() { return area; }
        public String getHawker() { return hawker; }
        public String getPaper() { return paper; }
    }

