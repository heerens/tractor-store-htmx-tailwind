# The Tractor Store - Micro Frontends with HTMX and Tailwind 

A micro frontends sample implementation of [The Tractor Store](https://micro-frontends.org/tractor-store/) built with Spring Boot + JTE, ESI, HTMX and Tailwind. 

It's a reference implementation to Alexander Heerens' article  [Radically Simple Web Architecture](https://medium.com/@alexander.heerens/radically-simple-web-architecture-a-web-application-blueprint-for-startups-and-small-enterprises-f503a5f36381) and based on the [Blueprint](https://github.com/neuland/tractor-store-blueprint).

**Live Demo:** [https://tractorstore.inauditech.com/](https://tractorstore.inauditech.com/)

## About This Implementation

### Architecture 

The architecture of the application is based on the [Radically Simple Web Architecture](https://medium.com/@alexander.heerens/radically-simple-web-architecture-a-web-application-blueprint-for-startups-and-small-enterprises-f503a5f36381) article.

<img src="https://miro.medium.com/v2/resize:fit:1400/format:webp/1*rJivU2mODY8LxD3tK60kPA.png" alt="Overview" width="800" >

There are two teams that both run a Modular Monolith (Spring Boot app) containing multiple Self Contained Systems (Module). Each SCS represents an independent business domain (DDD).

Routing, Page Assembly (Server-Side Integration) and Pattern Library hosting is part of a NGINX. 
 

### Technologies

List of techniques used in this implementation.

| Aspect                     | Solution                                    |
|----------------------------|---------------------------------------------|
| 🛠️ Frameworks, Libraries  | [spring-boot], [htmx], [tailwind]           |
| 📝 Rendering               | SSR templating with [jte]                   |
| 🐚 Application Shell       | None                                        |
| 🧩 Client-Side Integration | HTMX patical updates                        |
| 🧩 Server-Side Integration | ESI + Routing via [nginx]                   |
| 📣 Communication           | Custom Events + HTMX Attributes ([htmx])    |
| 🗺️ Navigation             | MPA, HTMX patical updates, Hard-Nav Between |
| 🎨 Styling                 | [tailwind] + Custom CSS                     |
| 🍱 Design System           | Demo Pattern Lib [pattern-lib]              |
| 🔮 Discovery               | None (Hardcoded URLs for Now)               |
| 🚚 Deployment              | AWS App Runner (Docker image)               |
| 👩‍💻 Local Development    | [docker-compose], [gradle]                  |

[spring-boot]: https://spring.io/projects/spring-boot
[jte]: https://jte.gg/
[htmx]: https://htmx.org/
[tailwind]: https://tailwindcss.com/
[nginx]: https://nginx.org/en/
[docker-compose]: https://docs.docker.com/compose/
[gradle]: https://gradle.org/
[pattern-lib]: https://tractorstore.inauditech.com/storybook/index.html

### Domain and Team Boundaries

The original Tractor Store has three teams (Explore, Decide, Checkout) and their team boundaries can be visualized in the shop via a toggle at the bottom.  

Our implementation is focused around four business domain run by two teams. This gives you an overview how the boundaries are defined:

| Original Team | Domain     | Team     |
|---------------|------------|----------|
| 🔴 Explore    | navigation | Discover |
| 🟢 Decide     | product    | Discover |
| 🟣 Inspire    | discovery  | Discover |
| 🟡 Checkout   | checkout   | Checkout |
| 🔵 n.a        | account    | Checkout |

And this is how one domain is including another. 

- 🔴 `navigation`
    - 📄 Home
    - 📄 Stores
    - 🧩 Header (🔴🟢🟡🟣 every page, except checkout)
    - 🧩 Footer (🔴🟢🟡🟣 every page)
    - 🧩 Store Picker (🟡 checkout)
- 🟣 `discovery`
    - 📄 Category
    - 🧩 Recommendations (🔴 home, 🟢 product detail, 🟡 cart)
    - 🧩 Store Picker (🟡 checkout)    - 
- 🟢 `product`
    - 📄 Product detail
- 🟡 `checkout`
    - 📄 Cart
    - 📄 Checkout
    - 📄 Thank you
    - 🧩 Mini Cart (🔴 header)
    - 🧩 Add To Cart Button (🟢 product details)


### Limitations & Remarks 

This implementation is deliberately kept simple to focus on the micro frontends aspects. URLs are hardcoded, components could be more DRY and no linting, testing or type-safety is implemented. In a real-world scenario, these aspects should be addressed properly.

#### Tailwind v2

For the use of Tailwind v3+ each domain must create its own css files during the build and SCSs must include each other's resources.

#### Data Replication

Product data is provided by the `product` domain and pull via data feed (https://tractorstore.inauditech.com/product/api/v1/feed) by other domain such as `checkout`.

The demo `checkout` pulls all data on startup and holds data in memory. A robust production application would run a periodic job, pull only updates and persist data.

#### One Docker Image

For demo purposes the `Dockerfile` will create an image containing and running the two Spring apps and the NGINX. In a real-word scenario these three would have their own pipelines, docker images and deployments. 

#### JTE

Because the Modular Monolith is one Spring Boot app, all modules share one class namespace. That means all classes, packages and JTE templates need to have a namespace to avoid conflicts.

By default, JTE does not support multiple template directories (modules), and hab to create an own `JteModuleConfiguration` to make the possible.

## How to run locally

Clone this repository and run the following commands:

```bash
git clone https://github.com/heerens/tractor-store-htmx-tailwind
cd tractor-store-htmx-tailwind
```

Build the app(s):

```bash
# build the app + nginx image
docker-compose build 
```

Start the development server:

```bash
docker-compose up 
```

Open http://localhost:3000 in your browser to see the integrated application.

## About The Author

Alex is a founder, architect and full stack developer at [Inaudi Tech](https://www.inauditech.com/). He has worked the last 20+ years in web application development and helped small start-ups as well as big e-commerce companies to find the right tech stack for their missions.