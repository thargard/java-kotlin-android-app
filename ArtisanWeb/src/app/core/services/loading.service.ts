import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

/**
 * Сервис для управления индикатором загрузки
 */
@Injectable({
  providedIn: 'root',
})
export class LoadingService {
  private loadingSubject = new BehaviorSubject<boolean>(false);
  private requestCount = 0;

  /**
   * Observable для подписки на состояние загрузки
   */
  public loading$: Observable<boolean> = this.loadingSubject.asObservable();

  /**
   * Показать индикатор загрузки
   */
  show(): void {
    this.requestCount++;
    if (this.requestCount === 1) {
      this.loadingSubject.next(true);
    }
  }

  /**
   * Скрыть индикатор загрузки
   */
  hide(): void {
    this.requestCount--;
    if (this.requestCount <= 0) {
      this.requestCount = 0;
      this.loadingSubject.next(false);
    }
  }

  /**
   * Проверить, идет ли загрузка
   */
  isLoading(): boolean {
    return this.loadingSubject.value;
  }
}
