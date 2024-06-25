package ru.open.work3;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;

public class Utils {
	public static long startTime = System.nanoTime(); // Для вывода тайминга от момента запуска
	public static double durTime() {return ((System.nanoTime() - startTime) /1000000)/1000.;}

	public static <T> T cache(T object) {
		Class cls = object.getClass();
		return (T) Proxy.newProxyInstance(
				cls.getClassLoader(),
				cls.getInterfaces(),
				new ObjInvHandler(object)
		);
	}

	// Обработчик вызова метода
	static class ObjInvHandler implements InvocationHandler
	{
		private final Object obj;
		ObjInvHandler(Object obj){this.obj = obj;}

		// Массив кэшей результатов выполнения методов для разных состояний объекта
		// (для внутреннего HashMap ключ String получаем методом getStringKey из CalcPowerDecorator)
		public HashMap<Method, HashMap<String, Object>> methCache2 = new HashMap<>();
		// Массив потоков, отслеживающих время жизни и чистящих кэш
		// аналог предыдущего, только вместо кэша - ссылка на поток, очищающий кэш через заданное время
		public HashMap<Method, HashMap<String, Thread>> threadCache2 = new HashMap<>();
		Object res;
		Method methStringKey;
		String stringKey; // ключ состояния объекта
		int sleepTime;

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

			Method m = obj.getClass().getMethod(method.getName(), method.getParameterTypes());
			// Ищем метод в массиве кэшей, если нет - заносим
			if(!methCache2.containsKey(m)) {
				methCache2.put(m, new HashMap<>());
				threadCache2.put(m, new HashMap<>());
			}
			methStringKey = obj.getClass().getMethod("getStringKey"); // В декораторе исходного класса должен быть этот метод
			stringKey = (String) methStringKey.invoke(obj, null);
			if(!methCache2.get(m).containsKey(stringKey)){
				methCache2.get(m).put(stringKey, null);
				threadCache2.get(m).put(stringKey, null);
			}

			boolean isCache = false;
			// Смотрим аннотации
//			Annotation[] anns = m.getDeclaredAnnotations();
//			for (Annotation a: anns){
//				if(a.annotationType().equals(Cache.class)) { // метод кэшируемый - берём кэш, если есть
//					isCache = true;
//					sleepTime = ((Cache)a).cacheTime();
//				}
//			}
			if (m.isAnnotationPresent(Cache.class)){
				isCache = true;
				sleepTime = ((Cache)m.getAnnotation(Cache.class)).cacheTime();
			}

			if(isCache){
				res = methCache2.get(m).get(stringKey);
				if(res != null){ // берём данные из кэша
					System.out.println(durTime() + ": " + res + " - ! Взято из кэша !");
					// Прерываем поток очистки кэша и запускаем новый (т.е., продлеваем время жизни)
					threadCache2.get(m).get(stringKey).interrupt(); // Прерываем текущий поток
					threadCache2.get(m).put(stringKey, new Thread(new ThrClearCache(m, stringKey, methCache2, sleepTime))); // Создаём новый поток очистки кэша
					threadCache2.get(m).get(stringKey).start(); // Запускаем поток
					return res;
				}
			}
			res = method.invoke(obj, args);
			if(isCache) {
				methCache2.get(m).put(stringKey, res); // Заносим в кэш
				threadCache2.get(m).put(stringKey, new Thread(new ThrClearCache(m, stringKey, methCache2, sleepTime))); // Создаём поток очистки кэша
				threadCache2.get(m).get(stringKey).start(); // Запускаем поток
			}
			return res;
		}
	}
}
