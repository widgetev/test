package ru.open.work3;

// Исходный класс, который нельзя менять
public class CalcPower {
	private double val, pow;
	public CalcPower(double val, double pow){
		this.val = val;
		this.pow = pow;
	}
	public double powerValue(){
		return Math.pow(val, pow);
	}
	public void setVal(double val) {
		this.val = val;
	}
	public void setPow(double pow) {
		this.pow = pow;
	}
	public double getVal() {
		return val;
	}
	public double getPow() {
		return pow;
	}
}
