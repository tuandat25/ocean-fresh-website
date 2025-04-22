import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';

// Layout components
import { HeaderComponent } from './components/layout/header/header.component';
import { FooterComponent } from './components/layout/footer/footer.component';

// Home components
import { HeroComponent } from './components/home/hero/hero.component';
import { FeaturedCategoriesComponent } from './components/home/featured-categories/featured-categories.component';
import { BestSellersComponent } from './components/home/best-sellers/best-sellers.component';
import { WhyChooseUsComponent } from './components/home/why-choose-us/why-choose-us.component';
import { TestimonialsComponent } from './components/home/testimonials/testimonials.component';
import { BlogPostsComponent } from './components/home/blog-posts/blog-posts.component';
import { ChatbotComponent } from './components/shared/chatbot/chatbot.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    HeaderComponent,
    FooterComponent,
    HeroComponent,
    FeaturedCategoriesComponent,
    BestSellersComponent,
    WhyChooseUsComponent,
    TestimonialsComponent,
    BlogPostsComponent,
    ChatbotComponent
  ],
  template: `
    <app-header></app-header>
    <main>
      <app-hero></app-hero>
      <app-featured-categories></app-featured-categories>
      <app-best-sellers></app-best-sellers>
      <app-why-choose-us></app-why-choose-us>
      <app-testimonials></app-testimonials>
      <app-blog-posts></app-blog-posts>
    </main>
    <app-footer></app-footer>
    <app-chatbot></app-chatbot>
  `,
  styleUrl: './app.component.scss'
})
export class AppComponent {
  title = 'Ocean Fresh';
}
