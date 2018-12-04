package app;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import util.DBWorker;

public class Person {
	
	// Данные записи о человеке.
	private String id = "";
	private String name = "";
	private String surname = "";
	private String middlename = "";
	private HashMap<String,String> phones = new HashMap<String,String>();
	private DBWorker db = DBWorker.getInstance();

	// Конструктор для создания записи о человеке на основе данных из БД. 
	public Person(String id, String name, String surname, String middlename)
	{
		this.id = id;
		this.name = name;
		this.surname = surname;
		this.middlename = middlename;
		
		// Извлечение телефонов человека из БД.
		ResultSet db_data = DBWorker.getInstance().getDBData("SELECT * FROM `phone` WHERE `owner`=" + id);
		
		try
		{
			// Если у человека нет телефонов, ResultSet будет == null.
			if (db_data != null)
			{
				while (db_data.next())
				{
					this.phones.put(db_data.getString("id"), db_data.getString("number"));
				}
			}	
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	// Конструктор для создания пустой записи о человеке.
	public Person()
	{
		this.id = "0";
		this.name = "";
		this.surname = "";
		this.middlename = "";
	}	

	// Конструктор для создания записи, предназначенной для добавления в БД. 
	public Person(String name, String surname, String middlename)
	{
		this.id = "0";
		this.name = name;
		this.surname = surname;
		this.middlename = middlename;
	}

	// Валидация частей ФИО. Для отчества можно передать второй параетр == true,
	// тогда допускается пустое значение.
	public boolean validateFMLNamePart(String fml_name_part, boolean empty_allowed)
	{
	    if (empty_allowed)
	    {
	    	Matcher matcher = Pattern.compile("^[а-яА-ЯёЁa-zA-Z0-9-_]{0,150}").matcher(fml_name_part);
	    	return matcher.matches();
	    }
	    else
	    {
	    	Matcher matcher = Pattern.compile("^[а-яА-ЯёЁa-zA-Z0-9-_]{0,150}").matcher(fml_name_part);
	    	return matcher.matches();
	    }
	}
	
	// ++++++++++++++++++++++++++++++++++++++
	// Геттеры и сеттеры
	public String getId()
	{
		return this.id;
	}
	
	public String getName()
	{
		return this.name;
	}

	public String getSurname()
	{
		return this.surname;
	}
	
	public String getMiddlename()
	{
		if ((this.middlename != null)&&(!this.middlename.equals("null")))
		{
			return this.middlename;
		}
		else
		{
			return "";
		}
	}

	public HashMap<String,String> getPhones()
	{
		return this.phones;
	}
	
	public void setId(String id)
	{
		this.id = id;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}

	public void setSurname(String surname)
	{
		this.surname = surname;
	}
	
	public void setMiddlename(String middlename)
	{
		this.middlename = middlename;
	}

	public void setPhones(HashMap<String,String> phones)
	{
		this.phones = phones;
	}
	// Геттеры и сеттеры
	// --------------------------------------

	public boolean addPhoneNumber(String number)
	{
		String query;

		query = "INSERT INTO `phone` (`owner`, `number`) VALUES ('" + id +"', '" + number +"')";

		Integer affected_rows = this.db.changeDBData(query);

		// Если добавление прошло успешно...
		if (affected_rows > 0)
		{
			phones.put(this.db.getLastInsertId().toString(), number);
			return true;
		}
		else
		{
			return false;
		}
	}

	// Обновление номера телефона.
	public boolean updatePhoneNumber(String id, String number)
	{
		Integer id_filtered = Integer.parseInt(id);
		String query = "";

		query = "UPDATE `phone` SET `number` = '" + number + "' WHERE `id` = " + id_filtered;

		Integer affected_rows = this.db.changeDBData(query);

		// Если обновление прошло успешно...
		if (affected_rows > 0)
		{
			phones.put(id, number);
			return true;
		}
		else
		{
			return false;
		}
	}


	// Удаление номера телефона.
	public boolean deletePhoneNumber(String id)
	{
		if ((id != null)&&(!id.equals("null")))
		{
			int filtered_id = Integer.parseInt(id);

			Integer affected_rows = this.db.changeDBData("DELETE FROM `phone` WHERE `id`=" + filtered_id);

			// Если удаление прошло успешно...
			if (affected_rows > 0)
			{
				phones.remove(id);
				return true;
			}
			else
			{
				return false;
			}
		}
		else
		{
			return false;
		}
	}
}
