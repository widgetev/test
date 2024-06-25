import lombok.SneakyThrows;
import ru.open.work3.CalcPower;
import ru.open.work3.CalcPowerDecorator;
import ru.open.work3.Powerable;
import ru.open.work3.Utils;

public class Main {
	@SneakyThrows
	public static void main(String[] args) {

		double res;

		CalcPowerDecorator calcPower = new CalcPowerDecorator(new CalcPower(5.0, 2.0));
		Powerable ch = Utils.cache(calcPower);

		System.out.println("");
		System.out.println(" ------ Проверка - кэш и разные состояния, продление жизни кэша после обращения к нему");

		res = ch.powerValue(); // 5|2 Рассчитается за 1 с, значение рассчитываетя
		res = ch.powerValue(); // 5|2 Рассчитается быстро, значение из кэша
		ch.setPow(4);
		res = ch.powerValue(); // 5|4 Рассчитается за 1 с, значение рассчитываетя
		res = ch.powerValue(); // 5|4 Рассчитается быстро, значение из кэша
		ch.setPow(2);
		res = ch.powerValue(); // 5|2 Рассчитается быстро, значение из кэша
		Thread.sleep(5500); // кэш очистился
		res = ch.powerValue(); // 5|2 Рассчитается за 1 с, значение рассчитываетя
		ch.setPow(4);
		res = ch.powerValue(); // 5|4 Рассчитается за 1 с, значение рассчитываетя


		Thread.sleep(5500); // кэш очистился
		System.out.println("");
		System.out.println(" ------ Проверка - потоки очистки кэша ожидают завершения долгого метода");

		ch.setPow(2);
		res = ch.powerValue(); // 5|2 Рассчитается за 1 с, запустится поток жизни кэша (5 с)
		ch.setPow(4);
		res = ch.powerValue(); // 5|4 Рассчитается за 1 с, запустится поток жизни кэша (5 с)
		res = ch.powerValueNc(); // блокируем всё на 10 секунд - потоки должны будут ждать завершения

	}

}
