import { Routes } from '@angular/router';
import { AllProductsComponent } from './components/all-products/all-products.component';

export const routes: Routes = [
  { path: '', component: AllProductsComponent },
  { path: 'products', component: AllProductsComponent },
  { path: '**', redirectTo: '' } // Redirect any unknown routes to home
];
