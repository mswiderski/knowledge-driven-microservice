package org.jbpm.demo.bank;

import java.io.Serializable;


public class LoanApplication implements Serializable {

    private static final long serialVersionUID = 3417007633797297505L;

    private String name;
    private String requestId;
    
    private double income;
    
    private double amount;
    private int lengthYears;
    
    private boolean approved;
    private String explanantion;
    
    public LoanApplication() {
    }
    
    public LoanApplication(String requestId, String name, double income, double amount, int lengthYears) {
        this.requestId = requestId;
        this.name = name;
        this.income = income;
        this.amount = amount;
        this.lengthYears = lengthYears;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getRequestId() {
        return requestId;
    }
    
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    
    public double getIncome() {
        return income;
    }
    
    public void setIncome(double income) {
        this.income = income;
    }
    
    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
    
    public int getLengthYears() {
        return lengthYears;
    }
    
    public void setLengthYears(int lengthYears) {
        this.lengthYears = lengthYears;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }
    
    public String getExplanantion() {
        return explanantion;
    }

    public void setExplanantion(String explanantion) {
        this.explanantion = explanantion;
    }

    @Override
    public String toString() {
        return "LoanApplication [name=" + name + ", requestId=" + requestId +
                ", income=" + income + ", amount=" + amount + ", lengthYears=" + lengthYears +
                ", approved=" + approved + ", explanantion=" + explanantion + "]";
    }


}
