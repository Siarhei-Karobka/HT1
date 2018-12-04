package app;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ManagePersonServlet extends HttpServlet {
	
	// Идентификатор для сериализации/десериализации.
	private static final long serialVersionUID = 1L;
	
	// Основной объект, хранящий данные телефонной книги.
	private Phonebook phonebook;
       
    public ManagePersonServlet()
    {
        // Вызов родительского конструктора.
    	super();
		
    	// Создание экземпляра телефонной книги.
        try
		{
			this.phonebook = app.Phonebook.getInstance(); //to
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}        
        
    }

    // Валидация ФИО и генерация сообщения об ошибке в случае невалидных данных.
    private String validatePersonFMLName(app.Person person)
    {
		String error_message = "";
		
		if (!person.validateFMLNamePart(person.getName(), false))
		{
			error_message += "Имя должно быть строкой от 1 до 150 символов из букв, цифр, знаков подчёркивания и знаков минус.<br />";
		}
		
		if (!person.validateFMLNamePart(person.getSurname(), false))
		{
			error_message += "Фамилия должна быть строкой от 1 до 150 символов из букв, цифр, знаков подчёркивания и знаков минус.<br />";
		}
		
		if (!person.validateFMLNamePart(person.getMiddlename(), true))
		{
			error_message += "Отчество должно быть строкой от 0 до 150 символов из букв, цифр, знаков подчёркивания и знаков минус.<br />";
		}
		
		return error_message;
    }

	// Валидация телефона
	private String validatePhoneNumber(String phone) {
		String error_message = "";

		Matcher matcher = Pattern.compile("^[+][0-9#-]{2,50}$").matcher(phone);
		boolean doesMatch = matcher.matches();
		if (!doesMatch) {
			error_message += "Телефон должен содержать от 2 до 50 символов (цифра, -, #) и начинаться с +.<br />";
		}
		return error_message;
	}
    
    // Реакция на GET-запросы.
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		// Обязательно ДО обращения к любому параметру нужно переключиться в UTF-8,
		// иначе русский язык при передаче GET/POST-параметрами превращается в "кракозябры".
		request.setCharacterEncoding("UTF-8");
		
		// В JSP нам понадобится сама телефонная книга. Можно создать её экземпляр там,
		// но с архитектурной точки зрения логичнее создать его в сервлете и передать в JSP.
		request.setAttribute("phonebook", this.phonebook);
		
		// Хранилище параметров для передачи в JSP.
		HashMap<String,String> jsp_parameters = new HashMap<String,String>();

		// Диспетчеры для передачи управления на разные JSP (разные представления (view)).
		RequestDispatcher dispatcher_for_manager = request.getRequestDispatcher("/ManagePerson.jsp");
        RequestDispatcher dispatcher_for_list = request.getRequestDispatcher("/List.jsp");
        RequestDispatcher dispatcher_for_phoneManager = request.getRequestDispatcher("/ManagePhone.jsp");

		// Действие (action) и идентификатор записи (id) над которой выполняется это действие.
		String action = request.getParameter("action");
		String id = request.getParameter("id");
		String phone_id = request.getParameter("phone_id");
		
		// Если идентификатор и действие не указаны, мы находимся в состоянии
		// "просто показать список и больше ничего не делать".
        if ((action == null)&&(id == null))
        {
        	request.setAttribute("jsp_parameters", jsp_parameters);
            dispatcher_for_list.forward(request, response);
        }
        // Если же действие указано, то...
        else
        {
        	switch (action)
        	{
        		// Добавление записи.
        		case "add":
        			// Создание новой пустой записи о пользователе.
        			Person empty_person = new Person();
        			
        			// Подготовка параметров для JSP.
        			jsp_parameters.put("current_action", "add");
        			jsp_parameters.put("next_action", "add_go");
        			jsp_parameters.put("next_action_label", "Добавить");
        			
        			// Установка параметров JSP.
        			request.setAttribute("person", empty_person);
        			request.setAttribute("jsp_parameters", jsp_parameters);
        			
        			// Передача запроса в JSP.
        			dispatcher_for_manager.forward(request, response);
        		break;
			
        		// Редактирование записи.
        		case "edit":
        			// Извлечение из телефонной книги информации о редактируемой записи.        			
        			Person editable_person = this.phonebook.getPerson(id);
        			
        			// Подготовка параметров для JSP.
        			jsp_parameters.put("current_action", "edit");
        			jsp_parameters.put("next_action", "edit_go");
        			jsp_parameters.put("next_action_label", "Сохранить");

        			// Установка параметров JSP.
        			request.setAttribute("person", editable_person);
        			request.setAttribute("jsp_parameters", jsp_parameters);
        			
        			// Передача запроса в JSP.
        			dispatcher_for_manager.forward(request, response);
        		break;
			
        		// Удаление записи.
        		case "delete":
        			
        			// Если запись удалось удалить...
        			if (phonebook.deletePerson(id))
        			{
        				jsp_parameters.put("current_action_result", "DELETION_SUCCESS");
        				jsp_parameters.put("current_action_result_label", "Удаление выполнено успешно");
        			}
        			// Если запись не удалось удалить (например, такой записи нет)...
        			else
        			{
        				jsp_parameters.put("current_action_result", "DELETION_FAILURE");
        				jsp_parameters.put("current_action_result_label", "Ошибка удаления (возможно, запись не найдена)");
        			}

        			// Установка параметров JSP.
        			request.setAttribute("jsp_parameters", jsp_parameters);
        			
        			// Передача запроса в JSP.
        			dispatcher_for_list.forward(request, response);
       			break;

				//***************************************************************************

//				Добавляем новый номер
				case "addPhoneNumber":
					// Создание новой пустой записи о пользователе.
					editable_person = this.phonebook.getPerson(id);

					// Создание новой пустой записи о номере пользователя.
					String empty_number = "";

					// Подготовка параметров для JSP.
					jsp_parameters.put("current_action", "addPhoneNumber");
					jsp_parameters.put("next_action", "add_go_PhoneNumber");
					jsp_parameters.put("next_action_label", "Добавить");

					// Установка параметров JSP.
					request.setAttribute("number", empty_number);
					request.setAttribute("person", editable_person);
					request.setAttribute("jsp_parameters", jsp_parameters);

					// Передача запроса в JSP.
					dispatcher_for_phoneManager.forward(request, response);
					break;

				//редактирование телефона
				case "editPhoneNumber":
					// Извлечение из телефонной книги информации о редактируемой записи.
					editable_person = this.phonebook.getPerson(id);

					//Извлечение редактируемого номера из списка номеров
					String editable_number = editable_person.getPhones().get(phone_id);

					// Подготовка параметров для JSP.
					jsp_parameters.put("current_action", "editPhoneNumber");
					jsp_parameters.put("next_action", "edit_go_PhoneNumber");
					jsp_parameters.put("next_action_label", "Сохранить");

					// Установка параметров JSP.
					request.setAttribute("number", editable_number);
					request.setAttribute("person", editable_person);
					request.setAttribute("jsp_parameters", jsp_parameters);

					// Передача запроса в JSP.
					dispatcher_for_phoneManager.forward(request, response);
					break;

				//удаление телефона
				case "deletePhoneNumber":
					// Извлечение из телефонной книги информации о редактируемой записи.
					editable_person = this.phonebook.getPerson(id);

					// Если запись удалось удалить...
					if (editable_person.deletePhoneNumber(phone_id))
					{
						jsp_parameters.put("current_action_result", "DELETION_SUCCESS");
						jsp_parameters.put("current_action_result_label", "Удаление выполнено успешно");
					}
					// Если запись не удалось удалить (например, такой записи нет)...
					else
					{
						jsp_parameters.put("current_action_result", "DELETION_FAILURE");
						jsp_parameters.put("current_action_result_label", "Ошибка удаления (возможно, запись не найдена)");
					}
					//возврат к странице редактирования записи о пользователе
					jsp_parameters.put("next_action", "edit_go");
					jsp_parameters.put("next_action_label", "Сохранить");


					// Установка параметров JSP.
					request.setAttribute("person", editable_person);
					request.setAttribute("jsp_parameters", jsp_parameters);

					// Передача запроса в JSP.
					dispatcher_for_list.forward(request, response);
					break;
       		}
        }
		
	}

	// Реакция на POST-запросы.
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		// Обязательно ДО обращения к любому параметру нужно переключиться в UTF-8,
		// иначе русский язык при передаче GET/POST-параметрами превращается в "кракозябры".
		request.setCharacterEncoding("UTF-8");

		// В JSP нам понадобится сама телефонная книга. Можно создать её экземпляр там,
		// но с архитектурной точки зрения логичнее создать его в сервлете и передать в JSP.
		request.setAttribute("phonebook", this.phonebook);
		
		// Хранилище параметров для передачи в JSP.
		HashMap<String,String> jsp_parameters = new HashMap<String,String>();

		// Диспетчеры для передачи управления на разные JSP (разные представления (view)).
		RequestDispatcher dispatcher_for_manager = request.getRequestDispatcher("/ManagePerson.jsp");
		RequestDispatcher dispatcher_for_list = request.getRequestDispatcher("/List.jsp");
		RequestDispatcher dispatcher_for_phoneManager = request.getRequestDispatcher("/ManagePhone.jsp");

		
		// Действие (add_go, edit_go) и идентификатор записи (id) над которой выполняется это действие.
		String add_go = request.getParameter("add_go");
		String edit_go = request.getParameter("edit_go");
		String id = request.getParameter("id");

		//Действия с телефонным номером
		String add_go_PhoneNumber = request.getParameter("add_go_PhoneNumber");
		String edit_go_PhoneNumber = request.getParameter("edit_go_PhoneNumber");
		String delete_go_PhoneNumber = request.getParameter("delete_go_PhoneNumber");

		
		// Добавление записи.
		if (add_go != null)
		{
			// Создание записи на основе данных из формы.
			Person new_person = new Person(request.getParameter("name"), request.getParameter("surname"), request.getParameter("middlename"));

			// Валидация ФИО.
			String error_message = this.validatePersonFMLName(new_person); 
			
			// Если данные верные, можно производить добавление.
			if (error_message.equals(""))
			{

				// Если запись удалось добавить...
				if (this.phonebook.addPerson(new_person))
				{
					jsp_parameters.put("current_action_result", "ADDITION_SUCCESS");
					jsp_parameters.put("current_action_result_label", "Добавление выполнено успешно");
				}
				// Если запись НЕ удалось добавить...
				else
				{
					jsp_parameters.put("current_action_result", "ADDITION_FAILURE");
					jsp_parameters.put("current_action_result_label", "Ошибка добавления");
				}

				// Установка параметров JSP.
				request.setAttribute("jsp_parameters", jsp_parameters);
	        
				// Передача запроса в JSP.
				dispatcher_for_list.forward(request, response);
			}
			// Если в данных были ошибки, надо заново показать форму и сообщить об ошибках.
			else
			{
    			// Подготовка параметров для JSP.
    			jsp_parameters.put("current_action", "add");
    			jsp_parameters.put("next_action", "add_go");
    			jsp_parameters.put("next_action_label", "Добавить");
    			jsp_parameters.put("error_message", error_message);
    			
    			// Установка параметров JSP.
    			request.setAttribute("person", new_person);
    			request.setAttribute("jsp_parameters", jsp_parameters);
    			
    			// Передача запроса в JSP.
    			dispatcher_for_manager.forward(request, response);
			}
		}
		
		// Редактирование записи.
		if (edit_go != null)
		{
			// Получение записи и её обновление на основе данных из формы.
			Person updatable_person = this.phonebook.getPerson(request.getParameter("id")); 
			updatable_person.setName(request.getParameter("name"));
			updatable_person.setSurname(request.getParameter("surname"));
			updatable_person.setMiddlename(request.getParameter("middlename"));

			// Валидация ФИО.
			String error_message = this.validatePersonFMLName(updatable_person); 
			
			// Если данные верные, можно производить добавление.
			if (error_message.equals(""))
			{
			
				// Если запись удалось обновить...
				if (this.phonebook.updatePerson(id, updatable_person))
				{
					jsp_parameters.put("current_action_result", "UPDATE_SUCCESS");
					jsp_parameters.put("current_action_result_label", "Обновление выполнено успешно");
				}
				// Если запись НЕ удалось обновить...
				else
				{
					jsp_parameters.put("current_action_result", "UPDATE_FAILURE");
					jsp_parameters.put("current_action_result_label", "Ошибка обновления");
				}

				// Установка параметров JSP.
				request.setAttribute("jsp_parameters", jsp_parameters);
	        
				// Передача запроса в JSP.
				dispatcher_for_list.forward(request, response);
			}
			// Если в данных были ошибки, надо заново показать форму и сообщить об ошибках.
			else
			{
    			// Подготовка параметров для JSP.
    			jsp_parameters.put("current_action", "edit");
    			jsp_parameters.put("next_action", "edit_go");
    			jsp_parameters.put("next_action_label", "Сохранить");
    			jsp_parameters.put("error_message", error_message);

    			// Установка параметров JSP.
    			request.setAttribute("person", updatable_person);
    			request.setAttribute("jsp_parameters", jsp_parameters);
    			
    			// Передача запроса в JSP.
    			dispatcher_for_manager.forward(request, response);
			}
		}

		//Добавление нового номера
		if (add_go_PhoneNumber != null)
		{
			// Извлечение записи, для редактирования телефона.
			Person editable_person = phonebook.getPerson(id);

			//Создание записи
			String new_number = request.getParameter("number");

			// Валидация номера.
			String error_message = this.validatePhoneNumber(new_number);

			// Если данные верные, можно производить добавление.
			if (error_message.equals(""))
			{

				// Если запись удалось добавить...
				if (editable_person.addPhoneNumber(new_number))
				{
					jsp_parameters.put("current_action_result", "ADDITION_SUCCESS");
					jsp_parameters.put("current_action_result_label", "Добавление выполнено успешно");
				}
				// Если запись НЕ удалось добавить...
				else
				{
					jsp_parameters.put("current_action_result", "ADDITION_FAILURE");
					jsp_parameters.put("current_action_result_label", "Ошибка добавления");
				}

				// Возврат к записи
				jsp_parameters.put("next_action", "edit_go");
				jsp_parameters.put("next_action_label", "save");

				// Установка параметров JSP.
				request.setAttribute("person", editable_person);
				request.setAttribute("jsp_parameters", jsp_parameters);

				// Передача запроса в JSP.
				dispatcher_for_phoneManager.forward(request, response);
			}
			// Если в данных были ошибки, надо заново показать форму и сообщить об ошибках.
			else
			{
				// Подготовка параметров для JSP.
				jsp_parameters.put("current_action", "addPhoneNumber");
				jsp_parameters.put("next_action", "add_go_PhoneNumber");
				jsp_parameters.put("next_action_label", "Добавить");
				jsp_parameters.put("error_message", error_message);

				// Установка параметров JSP.
				request.setAttribute("number", new_number);
				request.setAttribute("jsp_parameters", jsp_parameters);

				// Передача запроса в JSP.
				dispatcher_for_manager.forward(request, response);
			}
		}
		// Редактирование записи телефонного номера.
		if (edit_go_PhoneNumber != null)
		{
			//извлечение редактируемой записи
			Person editable_person = phonebook.getPerson(id);

			// Получение записи и её обновление на основе данных из формы.
			String updatable_number = request.getParameter("number");

			// Валидация номера.
			String error_message = this.validatePhoneNumber(updatable_number);

			// Если данные верные, можно производить добавление.
			if (error_message.equals(""))
			{

				// Если запись удалось обновить...
				if (editable_person.updatePhoneNumber(id, updatable_number))
				{
					jsp_parameters.put("current_action_result", "UPDATE_SUCCESS");
					jsp_parameters.put("current_action_result_label", "Обновление выполнено успешно");
				}
				// Если запись НЕ удалось обновить...
				else
				{
					jsp_parameters.put("current_action_result", "UPDATE_FAILURE");
					jsp_parameters.put("current_action_result_label", "Ошибка обновления");
				}

				//возврат к записи
				jsp_parameters.put("next_action", "edit_go");
				jsp_parameters.put("next_action_label", "save");

				// Установка параметров JSP.
				request.setAttribute("person", editable_person);
				request.setAttribute("jsp_parameters", jsp_parameters);

				// Передача запроса в JSP.
				dispatcher_for_phoneManager.forward(request, response);//todo
			}
			// Если в данных были ошибки, надо заново показать форму и сообщить об ошибках.
			else
			{
				// Подготовка параметров для JSP.
				jsp_parameters.put("current_action", "editPhoneNumber");
				jsp_parameters.put("next_action", "edit_go_phoneNumber");
				jsp_parameters.put("next_action_label", "Сохранить");
				jsp_parameters.put("error_message", error_message);

				// Установка параметров JSP.
				request.setAttribute("number", updatable_number);
				request.setAttribute("jsp_parameters", jsp_parameters);

				// Передача запроса в JSP.
				dispatcher_for_phoneManager.forward(request, response);
			}
		}
	}
}
