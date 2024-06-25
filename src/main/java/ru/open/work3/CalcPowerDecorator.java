package ru.open.work3;

import lombok.SneakyThrows;

// Считаем, что исходный класс менять нельзя, поэтому оборачиваем его в этот декоратор
// и здесь добавляем аннотации и метод getStringKey
public class CalcPowerDecorator implements Powerable {
	public static final Object mon = new Object(); // Для пробуждения потоков очистки кэша

	private final CalcPower baseCalcPower;

	public CalcPowerDecorator(CalcPower baseCalcPower){
		this.baseCalcPower = baseCalcPower;
	}

	@SneakyThrows
	@Cache(cacheTime = 5000)
	public double powerValue(){
		System.out.println(Utils.durTime() + ": " + "    ===> начало выполнения powerValue " + getStringKey());
		ThrClearCache.isMethRunning = true;
		Thread.sleep(1000); // имитация "тяжёлого" метода
		double res = baseCalcPower.powerValue(); // метод исходного класса
		System.out.println(Utils.durTime() + ": " + "    ===< завершение выполнения powerValue " + getStringKey());
		ThrClearCache.isMethRunning = false;
		synchronized (mon) { // сигнал ожидающим потокам, что можно просыпаться
			mon.notifyAll();
		}
		System.out.println(Utils.durTime() + ": " + res + " -  Вычислено в powerValue");
		return res;
	}

	// Аналогичный не кэшируемый метод для проверки
	@SneakyThrows
	public double powerValueNc(){
		System.out.println(Utils.durTime() + ": " + "    ===> начало выполнения powerValueNc " + getStringKey());
		ThrClearCache.isMethRunning = true;
		Thread.sleep(10000); // имитация "очень тяжёлого" метода (используем для проверки ожидания его завершения потоками очистки кэша)
		double res = baseCalcPower.powerValue(); // метод исходного класса
		System.out.println(Utils.durTime() + ": " + "    ===< завершение выполнения powerValueNc " + getStringKey());
		ThrClearCache.isMethRunning = false;
		synchronized (mon) { // сигнал ожидающим потокам, что можно просыпаться
			mon.notifyAll();
		}
		System.out.println(Utils.durTime() + ": " + res + " -  Вычислено в powerValueNc");
		return res;
	}

	@Mutator
	public void setVal(double val) {
		baseCalcPower.setVal(val);
	}
	@Mutator
	public void setPow(double pow) {
		baseCalcPower.setPow(pow);
	}

	// Получение строкового ключа состояния объекта исходного класса для хранения кэша.
	// Если бы у класса не было геттеров, можно было бы получить значения через рефлексию.
	// В формировании ключа состояния участвуют поля, помеченные аннотацией @Mutator
	// надо бы перенести эту логику в общем виде в Utils
	// только непонятно, как преобразовать поле произвольного типа в строку, однозначно идентифицирующую значение поля
	public String getStringKey(){
		return this.baseCalcPower.getVal() + "|" + this.baseCalcPower.getPow();
	}

}
