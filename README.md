<p align="center">
  <img src="app\src\main\res\drawable\ic_barmate.png" alt="Logo" width="200"/>
</p>

## 📖 Spis treści

1. [Opis projektu](#l1)
2. [Główne funkcjonalności](#l2)
3. [Technologie i architektura](#l3)
4. [Interfejs użytkownika](#l4)

<a id="l1"></a>

## 📋 Opis projektu

Aplikacja mobilna wspierająca użytkownika w przygotowywaniu koktajli, stworzona w Android Studio z użyciem języka Kotlin przy pomocy Jetpack Compose.

<a id="l2"></a>

## 🛠️ Główne funkcjonalności
* Szczegóły drinka wraz z krótkim opisem przygotowania prezentowane po wybraniu z listy
* Licznik z animacją shakera barmańskiego, reagujący na stan aplikacji
* Dynamiczne zarządzanie stanem interfejsu użytkownika dzięki wykorzystaniu ViewModel
* Wyświetlanie listy drinków z lokalnej bazy danych
* Obsługa przechowywania i odczytu danych przy użyciu Room (SQLite)
* Widoki dostosowane do orientacji urządzenia zarówno na telefonie jak i tablecie

<a id="l3"></a>

## ⚙️ Technologie i architektura
* **Kotlin**
* **Jetpack Compose:** deklaratywny framework, nowoczesne podejście do tworzenia UI
* **MVVM (Model-View-ViewModel):** wzorzec architektoniczny oddzielający logikę biznesową od warstwy UI
* **Room:** biblioteka ORM do zarządzania lokalną bazą danych SQLite w sposób obiektowy
* **mutableStateOf:** mechanizm zarządzania stanem w Compose, umożliwiający automatyczne odświeżanie UI
* **Material3:** nowoczesny system projektowania UI z komponentami zgodnymi z najnowszymi wytycznymi Google
  
<a id="l4"></a>

## 🖥️ Interfejs użytkownika 

<details>
  <summary>📱 Ekran powitalny </summary>
  <img src="SmartTemp-fe/images/logowanie.png" alt="Splash screen"/>
</details>

<details>
  <summary>📋 Widok listy koktajli </summary>
  <img src="SmartTemp-fe/images/wykres.png" alt="Widok listy koktajli"/>
</details>

<details>
  <summary>📇 Szczegóły drinka </summary>
  <img src="SmartTemp-fe/images/panel.png" alt="Szczegóły drinka"/>
</details>

<details>
  <summary>⌛ Mierzenie czasu (licznik) </summary>
  <img src="SmartTemp-fe/images/full_view.png" alt="Mierzenie czasu (licznik)"/>
</details>
