### 11/18/2024

#### Dare Mighty Things: What NASA's Bold Endeavors Teach Us About the Power of Calculated RISCs
https://qconsf.com/keynote/nov2024/dare-mighty-things-what-nasas-bold-endeavors-teach-us-about-power-calculated-riscs

Imagine building a car-sized-rover, shipping it 225 million kilometers away, and landing it on its wheels - on Mars! This is not science fiction. It is precisely what NASA Jet Propulsion Laboratory did with the Curiosity Rover. Armed with a nuclear power plant and lasers, this magnificent vehicle drives autonomously on the rough martian terrain. The onboard processor is a 200 MHz RAD750, a radiation hardened version of what you would find in an Apple computer from 1990s.

This keynote explores the essence of bold, calculated risks. We’ll discuss why taking these risks is often safer than playing it safe, how to identify and mitigate potential downsides, and the characteristics of good risks. We will also cover how to identify the risks that we take inadvertently and how being deliberate about those can change your outlook on being more innovative in your daily lives.

We'll delve into NASA's bold move to put a Qualcomm Snapdragon chip on a helicopter on Mars. If you are using an Android phone or tablet, you likely have the same ARM chip in your pocket. The amount of science this chip can do compared to the RAD750 is astounding - and it is a clear example of why this leap in technology wasn’t just a risk but a necessity to advance our understanding of the universe.

#### Systems Thinking for Building Resilient Engineering Organizations
https://qconsf.com/presentation/nov2024/systems-thinking-building-resilient-engineering-organizations

The introduction of mainstream LLMs is not only changing the products we build, it's also changing the way we build those products, the way we hire and the way we build our teams. As a leader it’s more important than ever to build and enable a strong engineering culture that allows you to steer your team and technology through all the changes and come out stronger on the other side.

This talk will focus on how to use systems thinking to build a resilient engineering organization. Providing strategies and tactics to reduce chaos, increase predictability, setting your team to take on any industry change, without burning out in the process.

#### What to Pack for Your GenAI Adventure
https://qconsf.com/presentation/nov2024/what-pack-your-genai-adventure

Get ready for an exciting adventure into the uncharted territory of GenAI product development! This presentation will unpack the essential tools and skills you need to thrive in this new era. First, we'll review your product management tools, emphasizing how core skills like understanding user needs, solving real problems, and delivering unique value still apply in the GenAI world. Next, we'll delve into the specific considerations for building with LLMs: defining requirements around multi-modality, balancing accuracy and hallucinations, addressing ethical implications, and incorporating user feedback loops. Finally, we'll explore the iterative LLM development cycle – from curating datasets, crafting prompts, and incorporating relevant data, to fine-tuning models and evaluating results – guiding you through each stage with practical tips and real-world examples that will help you move from prototype to launch.

Get ready to pack your bags with knowledge, strategies, and inspiration to build GenAI products that push the boundaries of what's possible. Your GenAI adventure starts here!

#### Recommender and Search Ranking Systems in Large Scale Real World Applications
https://qconsf.com/presentation/nov2024/recommender-and-search-ranking-systems-large-scale-real-world-applications

Recommendation and search systems are two of the key applications of machine learning models in industry. Current state of the art approaches have evolved from tree based ensembles models to large deep learning models within the last few years. This brings several modeling, systems, infrastructural and software challenges and improvements with it. Additionally, Large Language Models and Foundation Models are also rapidly starting to influence the capabilities of these real world search and recommender systems.

In this talk, Moumita will present an overview of industry search and recommendations systems, go into modeling choices, data requirements and infrastructural requirements, while highlighting challenges typically faced for each and ways to overcome them.


#### Exploring the Unintended Consequences of Automation in Software
https://qconsf.com/presentation/nov2024/exploring-unintended-consequences-automation-software

Automation is ubiquitous—it is entwined in our daily lives in ways that we aren’t always aware of. It has been woven into all aspects of modern software by being presented as a utopian vision: a way of making human lives easier, doing repetitive tasks faster and with fewer errors, freeing us fallible humans up to do other ostensibly more important work. But anyone who has worked directly with automated systems knows that we are still very far from such a dreamy reality.

This talk delves into detailed research about how automation is involved in software incidents. My focus on this area stems from the growing portrayal of automation as a panacea for various software incident issues, despite its limitations in effectively addressing these challenges, such as reliable detection and resolution of software issues or analyzing and disseminating learnings from these incidents back into the organization and its products and services.

Drawn directly from public incident reports (collected in the VOID), the research revealed multiple, often competing, roles that automation can play over the course of an incident, and most importantly underscored how important humans are at understanding, troubleshooting, and recovering from automated software issues. It’s a slightly vexing perspective, but all is not lost! I close with research-backed suggestions for ways we can coexist and build better systems in partnership with automation.


#### How GitHub Copilot Serves 400 Million Completion Requests a Day
https://qconsf.com/presentation/nov2024/how-github-copilot-serves-400-million-completion-requests-day

GitHub Copilot is the largest LLM powered Code Completion service in the world, serving hundreds of millions of requests per day with an average response time of under 200ms. This is the story of the architecture which powers this product.


#### Legacy Modernization: Architecting Real-Time Systems Around a Mainframe
https://qconsf.com/presentation/nov2024/legacy-modernization-architecting-real-time-systems-around-mainframe

Designing systems that take advantage of modern platforms, tools, and techniques is critical for building scalable, evolvable applications that underpin businesses of all stripes. Leveraging those when your data is captured in a mainframe, which does not scale well, is challenging. Technical and architectural debt is often a stifling element and the momentum to modernize can be both hard to gain and to maintain.

This talk discusses how National Grid built an event driven system using change data capture on top of a DB2 mainframe to power a cloud native GraphQL API hosted in Azure.

National Grid uses mainframes to power their customer billing capabilities. These mainframes encompass decades of data and capabilities in a monolithic, inelastic system. Many business and customer channels rely on this data and functionality. The motivating application for our project is the customer web portal. Earlier iterations of the portal relied on direct synchronous calls to the mainframe. This constraint historically caused problems for scaling to their customer base’s needs on the web. Availability and performance were key challenges.

Our architecture works by disconnecting the needs of the web application from the constraints of the mainframe. This ‘system of reference’ is hosted in the cloud with modern tech and is far better suited to adapting to business and (by extension) customer needs. It is a foundation which can, and is, being built upon to modernize National Grid’s technology portfolio, with no direct dependency on the legacy systems underlying them.

Change data capture enables near-real time synchronization
The ‘Customer Graph’ system-of-reference provides capability to scale independently from mainframe and be resilient to on-prem volatility
Asynchronous boundaries between cloud/on-prem resources decouples applications from connectivity/availability issues
APIs developed with domain-driven design, providing multi-channel support for diverse business applications

### 11/19/2024

#### The (Not So) Hidden Social Drivers Behind the Highest Performing Engineering Teams
https://qconsf.com/keynote/nov2024/not-so-hidden-social-drivers-behind-highest-performing-engineering-teams

Engineering teams are in constant search of the right signals or metrics of high-performing teams. But what if one of the most critical signals isn’t in your code commits, but in the interactions between teammates? This talk delves into how trust and psychological safety serve as powerful signals of team success. We'll explore practical methods to evaluate and measure these key social dimensions, and discuss their profound impact on the performance, innovation, and productivity of engineering teams. Additionally, we’ll provide actionable strategies for fostering and maintaining these essential elements, ensuring your team not only meets but exceeds its potential. 

#### Productivity Lessons in Moving from Big Tech to Scaling a Startup
https://qconsf.com/presentation/nov2024/productivity-lessons-moving-big-tech-scaling-startup

Most productivity research happens in large tech companies. Many of the lessons learned there apply to the world of Startups as well, but some don't carry over.

After years in big tech leading organizations at GitHub and Google, Rachel will discuss her transition to leading engineering for a fast-growing Startup. She has spent years focused on developer productivity including managing the team focused on Engineering Productivity Research at Google. She now spends her time as SVP leading Engineering for Sanity.io and doing Startup advising.

This talk includes tips on how adjusting your leadership style to meet the challenge of building productive and happy teams in the fast moving world of Startups.


#### Inflection Points in Engineering Productivity as Amazon Grew 30x
https://qconsf.com/presentation/nov2024/inflection-points-engineering-productivity-amazon-grew-30x

I joined Amazon in 2009, when we were a 3000-engineer company, and got to experience its fast growth from 3k to 90k engineers over a 15-yr period. During that time, our engineering productivity needs had a number of interesting inflection points. A little toil here and there was a minor annoyance when we were 3k engineers but it became a much larger bottleneck when we were 10k, and things that didn’t matter with 10k engineers mattered greatly with 90k engineers. We had to continuously evolve the way we thought about our development practices as the company grew.

Having worked at Google as well exposed me to how some fundamentally different architectural decisions these companies made shaped the way they test and release software in significantly distinct ways. For example, Google chose to maintain a single shared monorepo where more than a hundred thousand engineers work with no branches, whereas Amazon chose an architecture with tens of thousands of independent microrepos. Both systems work at scale but require very different investments in engineering productivity, such as how test environments fit in the picture, and what kind of testing happens before and after submit.

Join me and learn about my journey as I saw Amazon grow by 30x!

#### Platform Engineering: Evolution, Trends, and Future Impact on Software Delivery
https://qconsf.com/presentation/nov2024/platform-engineering-evolution-trends-and-future-impact-software-delivery

Platform Engineering is frequently hailed as the latest paradigm shift enabling developers to ship code faster - but is it simply a return to the pre-DevOps era of siloed teams focusing on narrow parts of the software lifecycle?

Over the last decade we've seen DevOps explode in popularity, only to be declared "dead". We've seen Platform-as-a-Service (PaaS) emerge as a game-changer, to then be surpassed in popularity by Kubernetes and developers building their own internal platforms from scratch.

In this presentation, Paula will share her unique insights and experience from working in this domain for more than 12 years, with a focus on:

how platform engineering has evolved over the last decade and the impact this has had on the practice of writing code
the latest trends around inner sourcing and enabling developer teams to directly contribute components and capabilities into the internal platform
practical takeaways on evaluating internal platform maturity and leveraging internal platforms to deliver code that is faster, safer and easier to maintain

#### Supporting Engineering Productivity for All
https://qconsf.com/presentation/nov2024/supporting-engineering-productivity-all

Understanding what drives software development productivity is the key to making high-impact investments in engineering productivity. For instance, research shows that high quality engineering managers, good documentation, and teams’ ability to resolve conflicts quickly are all associated with engineers’ productivity.

But not every engineer experiences these productivity factors in similar ways, especially engineers from historically marginalized groups.

In this talk, Dr. Emerson Murphy-Hill discusses what recent research on engineering productivity tells us about the surprising inequities in software engineering and what we can do about them, including:

What works for building and sustaining highly diverse engineering teams.
How putting authors’ faces at the top of technical documentation activates ageism in readers.
How women experience more interpersonal conflict during code review than men, and how implementing anonymous code review in your team can help.


#### Efficient Incremental Processing with Netflix Maestro and Apache Iceberg
https://qconsf.com/presentation/nov2024/efficient-incremental-processing-netflix-maestro-and-apache-iceberg

Incremental processing, an approach that processes only new or updated data in workflows, substantially reduces compute resource costs and execution time, leading to fewer potential failures and less need for manual intervention. However, enabling incremental processing on large-scale data pipelines and workflows presents significant challenges around scalability, ease of adoption, and user experience. In this talk, we will discuss how we are leveraging Apache Iceberg and Netflix Maestro to build an Incremental Processing Solution (IPS) that enables incremental processing of only new or changed data, reducing compute costs and processing times while ensuring data accuracy and freshness. By combining Iceberg's metadata capabilities for snapshots and data files with Maestro's workflow orchestration, we can efficiently handle late-arriving data and backfills in various different scenarios beyond append-only mode.

We will share our experiences and insights into how this IPS has empowered our data engineering teams to build more reliable, efficient, and scalable data pipelines, unlocking new data processing patterns. Through real-world use cases, we will demonstrate how IPS has significantly improved resource utilization, reduced execution times, and simplified pipeline management, all while maintaining data integrity. Additionally, we will discuss the emerging incremental processing patterns that we have discovered, such as using captured change data for row-level filtering and leveraging range parameters in business logic, as well as the techniques, best practices, and lessons learned from our journey towards incremental processing at Netflix.

#### Slack's AI-Powered, Hybrid Approach for Large-Scale Migration from Enzyme to React Testing Library
https://qconsf.com/presentation/nov2024/slacks-ai-powered-hybrid-approach-large-scale-migration-enzyme-react-testing

With the Enzyme test framework no longer supporting React 18, migrating to React Testing Library (RTL) became imperative.

At Slack, our hybrid approach integrated an Abstract Syntax Tree (AST) method and a Large Language Model (LLM) using Anthropic's AI model, Claude 2.1. Despite initial hurdles, we achieved an 80% conversion success rate.

Key innovations included AST conversions and annotations, DOM tree collection, stringent control mechanisms, and packaging all information into a cohesive pipeline with LLM call and feedback steps. This resulted in a 64% adoption rate and a 22% time-saving in test case conversion.

This success underscores the value of AI in large-scale code migrations and establishes a robust, innovative approach for similar challenges.

### 11/20/2024
#### Prompt Engineering: Is it a New Programming Language?
https://qconsf.com/keynote/nov2024/prompt-engineering-it-new-programming-language

For decades, we've been taught that programming requires writing lines of code, debugging, and testing. But what if there's a new way to program, one that doesn't require coding at all? Welcome to the world of prompt engineering, where language models and AI systems can generate code, create conversational interfaces, and even write games – all from simple text prompts. In this thought-provoking keynote, we'll challenge the conventional wisdom of programming and explore the possibilities of prompt engineering. Through live demos, real-world examples, and a dash of humor, we'll examine the similarities between prompt engineering and traditional programming, from designing and architecting complex systems to debugging and testing.

#### Thinking Like an Architect
https://qconsf.com/presentation/nov2024/thinking-architect

Are architects supposed to be the smartest people on the team, making all the important decisions for developers to fill in the blanks? Certainly not. Rather, architects make everyone else smarter, for example by sharing decision models or revealing blind spots. Architects also communicate across many organizational layers by using models and metaphors. This talk reflects on two decades working as an architect, ranging from the executive penthouse to the serverless engine room.

#### Engineering Influence and Forgetting the Soft Skills
https://qconsf.com/presentation/nov2024/engineering-influence-and-forgetting-soft-skills

Soft skills are overrated and dangerously oversold! The stigma with soft skills comes from the soft definition of these skills and gets confounded broadly with "being a people's person."  What if you could engineer getting more influence, driving a stronger culture, and being more effective as a leader? This talk takes a hard look into the biology of the brain and using the psychological underpinnings to make us truly exceptional. We will explore the relationship between AI and leadership by drawing insights from Work and Organizational Psychology, Evolutionary Psychology, and Neuropsychology. You will walk away with new, concrete tools to apply in your daily interactions to increase collaboration, commitment, and ownership across your organization.

#### Renovate to Innovate: Fundamentals of Transforming Legacy Architecture
https://qconsf.com/presentation/nov2024/renovate-innovate-fundamentals-transforming-legacy-architecture

Renovating old buildings and homes is commonplace, but why is technological renovation often overlooked? Just like a big home renovation adds to the quality of life, a successful architectural renovation has an outsized impact on the pace of innovation. Yet, why are software migrations perceived negatively? Frequently, it stems from past encounters with projects that were disruptive, costly, and executed poorly. In this talk, I outline my learnings on gracefully outgrowing technology and architectural choices, based on my experience scaling payment orchestration at Netflix to 250M members and preparing for the next 250M. You'll leave this session equipped with cognitive frameworks for evaluating architectural health and tactics to overcome common hurdles to transforming legacy architecture. I share battle-tested strategies for successfully navigating a ground-up architectural revamp to unlock innovation and enhance business value.

Key Takeaways:
Evaluate the architectural health of software systems
Identify and solve hurdles to transforming legacy architecture
Recognize strategies for a successful software architectural overhaul

#### Slack's Migration to a Cellular Architecture
https://qconsf.com/presentation/nov2024/slacks-migration-cellular-architecture

Cellular service architectures are a conceptually simple way for highly available online services to limit the impact of cascading failures and improve scale-out. So why aren't we all using them? And how do they even work in practice? 

In this talk, we'll explore Slack's transformative 18-month journey from a traditional multi-AZ architecture to a robust cellular architecture. Triggered by a critical incident in June 2021, this architectural shift revolutionized Slack's approach to system resilience and failure mitigation. We'll delve into the motivation behind the work, the operational characteristics of our particular implementation, challenges and complexities posed by the migration, and limitations of our approach as compared to others.

Attendees will gain insights into designing for failure in large-scale distributed systems, techniques for graceful degradation and traffic management, balancing consistency requirements with availability in cellular architectures, and strategies for executing major architectural changes while maintaining service quality.


#### Code quality in the AI Era is Needed More than Ever
https://qconsf.com/presentation/nov2024/code-quality-ai-era-needed-more-ever

As developers, we participate every day in our software life cycle by adding new logic, adapting existing code, and integrating with services or platforms. 

But as we start to add code generated by AI, how do we ensure that code quality meets the same standards we set for the code we write ourselves? How do we ensure our software is robust, maintainable, consistent, secure, clear, and tested? How do we maintain trust in the software we commit? 

In this session, I will discuss the need for good practices in Clean Code, demonstrate concepts like Clean As You Code (CAYC) using free and open-source tools, and discuss best practices for integrating AI code generation into developer workflows. 