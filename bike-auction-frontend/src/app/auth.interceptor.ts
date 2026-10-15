
import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const token = localStorage.getItem('bike_auction_token');

  if (
    !token ||
    !request.url.startsWith(
      'https://bike-auction-platform667.onrender.com/api/'
    )
  ) {
    return next(request);
  }

  return next(
    request.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    })
  );
};
