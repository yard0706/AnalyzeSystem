document.addEventListener('DOMContentLoaded', function() {
  const toggleBtn = document.getElementById('toggleSidebar');
  const sidebar = document.getElementById('sidebar');
  const content = document.querySelector('.content');

  // Проверяем состояние в localStorage
  const isSidebarHidden = localStorage.getItem('sidebarHidden') === 'true';

  // Устанавливаем начальное состояние
  if (isSidebarHidden) {
    sidebar.classList.add('sidebar-hidden');
    content.classList.add('content-expanded');
    toggleBtn.textContent = '☰';
  }

  // Обработчик клика на кнопку
  toggleBtn.addEventListener('click', function() {
    sidebar.classList.toggle('sidebar-hidden');
    content.classList.toggle('content-expanded');

    // Сохраняем состояние в localStorage
    const isNowHidden = sidebar.classList.contains('sidebar-hidden');
    localStorage.setItem('sidebarHidden', isNowHidden);

    // Меняем иконку кнопки
    toggleBtn.textContent = isNowHidden ? '☰' : '×';
  });

  // Адаптация к изменению размера окна
  window.addEventListener('resize', function() {
    if (window.innerWidth > 768) {
      sidebar.style.position = 'relative';
      sidebar.style.height = 'auto';
    } else {
      if (!sidebar.classList.contains('sidebar-hidden')) {
        sidebar.style.position = 'absolute';
        sidebar.style.height = 'calc(100vh - 120px)';
      }
    }
  });
});