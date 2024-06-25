package ru.open.work3;

import java.lang.reflect.Method;
import java.util.HashMap;

// Поток очистки кэша
// если по истечении времени жизни кэша выполняется "тяжёлый" метод (которая поднимает флаг isMethRunning),
// то поток ожидает завершения этого метода
public class ThrClearCache implements Runnable{
	public static volatile boolean isMethRunning; // Флаг работы "тяжёлого" метода в основном потоке
	private final Method m;
	private final String stringKey;
	private final HashMap<Method, HashMap<String, Object>> methCache2; // ссылка на массив с кэшами
	int sleepTime;
	public ThrClearCache(Method m, String stringKey, HashMap<Method, HashMap<String, Object>> methCache2, int sleepTime){
		this.m = m;
		this.stringKey = stringKey;
		this.methCache2 = methCache2;
		this.sleepTime = sleepTime;
	}
	@Override
	public void run() {
		System.out.println(Utils.durTime() + ": " + "    -----> поток запущен:                " + m + " " + stringKey + " " + this);
		if (sleepTime == 0) return; // никогда не чистим кэш
		try {
			Thread.sleep(sleepTime);
			synchronized(CalcPowerDecorator.mon)
			{
				// Проверяем, не выполняется ли "тяжёлый" метод, которому лучше не мешать
				if (isMethRunning) {
					System.out.println(Utils.durTime() + ": " + "!!! выполняется \"тяжёлый\" метод, ждём завершения !!! " + "(" + stringKey + ")");
					while (isMethRunning) {
						System.out.println(Utils.durTime() + ": " + "    *** wait       *** " + this);
						CalcPowerDecorator.mon.wait();
						System.out.println(Utils.durTime() + ": " + "    *** after wait *** " + this);
					}
				}
			}
		} catch (InterruptedException ex) {
			System.out.println(Utils.durTime() + ": " + "    -----X поток прерван, кэш не очищен: " + m + " " + stringKey + " " + this);
			return; // ничего не чистим, т.е., продлеваем время жизни
		}
		methCache2.get(m).put(stringKey, null); // чистим кэш
		System.out.println(Utils.durTime() + ": " + "    -----< поток остановлен, кэш очищен: " + m + " " + stringKey + " " + this);
	}
}
