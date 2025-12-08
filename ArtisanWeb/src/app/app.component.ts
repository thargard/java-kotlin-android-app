import { Component, OnInit } from '@angular/core';
import { TranslateService, TranslateModule } from '@ngx-translate/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrl: './app.component.css',
  standalone: true,
  imports: [CommonModule, TranslateModule],
})
export class AppComponent implements OnInit {
  title = 'ArtisanWeb';
  currentLang = 'en';

  constructor(private translate: TranslateService) {
    // Устанавливаем язык по умолчанию
    translate.setDefaultLang('en');
    // Используем английский язык
    translate.use('en');
  }

  ngOnInit() {
    // Можно получить язык из localStorage или браузера
    const browserLang = this.translate.getBrowserLang();
    const savedLang = localStorage.getItem('language');
    this.currentLang =
      savedLang || (browserLang?.match(/en|ru/) ? browserLang : 'en');
    this.translate.use(this.currentLang);
  }

  switchLanguage(lang: string) {
    this.currentLang = lang;
    this.translate.use(lang);
    localStorage.setItem('language', lang);
  }
}
