package ru.open.work3;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class UtilsTest {
	@SneakyThrows
	@Test
	@DisplayName("@Cache annotations test (for different states)")
	public void testCache1() {
		// Проверяем кэширование
		TestCalc tC = new TestCalc(10);
		Cacheable tCCache = Utils.cache(tC);
		tCCache.getVal();
		Assertions.assertEquals(tC.cntC, 1);
		tCCache.getVal();
		Assertions.assertEquals(tC.cntC, 1);
		tCCache.setVal(20);
		tCCache.getVal();
		Assertions.assertEquals(tC.cntC, 2);
		tCCache.getVal();
		Assertions.assertEquals(tC.cntC, 2);
		tCCache.setVal(10);
		tCCache.getVal();
		Assertions.assertEquals(tC.cntC, 2);
		Thread.sleep(3000); // кэш очищен
		tCCache.getVal();
		Assertions.assertEquals(tC.cntC, 3);
		tCCache.setVal(20);
		tCCache.getVal();
		Assertions.assertEquals(tC.cntC, 4);
		Thread.sleep(3000); // кэш очищен

		// А здесь кэш не работает
		tCCache.getValN();
		tCCache.getValN();
		Assertions.assertEquals(tC.cntM, 2);
	}
}

// Интерфейс для тестового класса
interface Cacheable {
	int getVal();
	int getValN();
	void setVal(int val);
}

// Тестовый класс, который будем обрабатывать через Utils
class TestCalc implements Cacheable {
	public int cntC = 0, cntM = 0;
	int val;
	TestCalc(int val){this.val = val;}
	@Cache(cacheTime=1000)
	public int getVal() {
		cntC++;
		System.out.println(Utils.durTime() + ": " + val + " -  Вычислено в getVal ");
		return val;
	}
	// Аналог без аннотации
	public int getValN() {
		cntM++;
		System.out.println(Utils.durTime() + ": " + val + " -  Вычислено в getValN ");
		return val;
	}

	public void setVal(int val){
		this.val = val;
	}
	public String getStringKey(){
		return String.valueOf(val);
	}
}

