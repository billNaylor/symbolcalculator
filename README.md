# Symbolic Calculator

Implemented symbolic operations on elementary functions and now supports:
- Some basic elementary functions: constant functions, power functions, exponential functions, logarithmic functions

- Some elementary functions: perform addition, subtraction, multiplication, division and compound operations on the above basic elementary functions.

- Calculation of differentials and partial derivatives of the above elementary functions

- Expression substitution

- Unconstrained optimisation (finding the minimum point of the expression)

- Inequality constrained optimisation

  > It is not an interior point method, so the effect may not be correct.

## User's guidance

### Type

Important types in the library include:

- Expression `Expression`

  An expression is an element of the set of all supported differentiable expressions.

- Variable `Variable`

  Variables indicate symbolic elements in operations.

### Entry and construction

With the help of rich extension functions, users can enter expressions naturally:

- define variables

  ```kotlin
  val x by variable // x is a variable named "x"
  val y by variable // y is a variable named "y"
  val variable by variable // variable is a variable named "variable"
  val t = Variable("ttt") // t is a variable named "ttt"
  
  // points defines the variable space of {x1, y1, z1, x2, ... , z5}
  val points by variableSpace("x", "y", "z", indices = 1..5)
  
  // xn is the variable space containing the three variables {xa, xb, xc}
  val xn by variableSpace("x", indices = listOf("a", "b", "c"))
  ```

  > Variable space: Variable space is an auxiliary type used to find gradients. The multivariate quantity function `y = f(x, y, z, ...)` can be regarded as a space `{x, y, z, ...} The quantity field on `, and its gradient on the space `{x, y, z, ...}` is expressed as `{∂f/∂x, ∂f/∂y, ∂f/∂z, .. .}`, if a certain variable `x` is regarded as a parameter, its gradient on the space `{y, z, ...}` can also be calculated `{∂f(x)/∂y, ∂f( x)/∂z, ...}`. Therefore, it is necessary to specify on which space the gradient is meaningful.


- Define expressions

  Here are some examples of expressions:

  ```kotlin
  val x by variable
  val y by variable
  
  val f1 = 9 * x * x * y + 7 * x * x + 4 * y - 2
  val f2 = ln(9 * x - 7)
  
  val f3 = sqrt((x `^` 2) + (y `^` 2))
  // `^` is the symbolic form of power, and can also be written as pow
  // Note that infix expressions have the lowest operation priority, lower than + and -, so parentheses must be added as components of sums and products.
  ```

  As long as it contains at least one unknown component or other expression, the entire object is automatically inferred as an expression.

  Expressions can be compounded within the scope of elementary functions:

  ```kotlin
  val f: Expression = ...
  val f1 = E `^` f // E === kotlin.math.E
  ```
  
  If it is necessary to define an expression without any unknowns (constant expression), use a constant expression:

  ```kotlin
  // Constant(Double) Convert a rational number to a constant expression
  val c1 = Constant(1.0 + 2 + 3)
  
  // This is also correct because the constant expression is also an expression component and c2 will be inferred as an expression
  val c2 = Constant(1.0) + 2 + 3
  ```

- differential

  In fact, differential is also an expression operation, which converts an expression into the expression of its differential:

  ```kotlin
  val x by variable
  val y by variable
  val f = (sqrt(x * x + y) + 1) `^` 2
  val df = d(f) 
  println(df) // Print：2 x dx + 2 (x^2 + y)^-0.5 x dx + dy + (x^2 + y)^-0.5 dy
  
  ...
  ```

  `dx`、`dy` It is a so-called microelement, which participates in operations as a factor that can be multiplied, divided and canceled.

  Differential operations eliminate the function part through the chain rule of derivation of sums, products and composite functions, retaining the microelements of variables.

  If `$u` and `$v` are two different variables, define `d d $u ≡ d$u / d$v ≡ d$v / d$u ≡ Constant(.0)`.

  Therefore, `d(f)/d(x)` is the partial derivative of `f(x,y)` with respect to `x`:

  ```kotlin
  ...
  
  val dfx = df / d(x)
  println(dfx) // Print：2 x + 2 (x^2 + y)^-0.5 x
  
  ...
  ```

  Multiple differentials can be saved and the cost of finding higher-order derivatives can be reduced:

  ```kotlin
  ...
  
  val ddf = d(df) // All differential operations are actually completed here. The so-called "partial derivative" is just the exponential addition and subtraction of differential terms.
  val dx = d(x)
  val dy = d(y)
  
  println("∂2f / ∂x2  = ${ddf / (dx * dx)}")
  println("∂2f / ∂x∂y = ${ddf / (dx * dy)}")
  println("∂2f / ∂y2  = ${ddf / (dy * dy)}")
  ```

  ```bash
  ∂2f / ∂x2  = 2 (x^2 + y)^-0.5 - 2 (x^2 + y)^-1.5 x^2 + 2
  ∂2f / ∂x∂y = -2 (x^2 + y)^-1.5 x
  ∂2f / ∂y2  = -0.5 (x^2 + y)^-1.5
  ```

- Substitute

  Substitution is the most common form of operations such as simplification and elimination.

  ```kotlin
  val x by variable
  val y by variable
  
  val f = x `^` 2
  println(f.substitute(x, 2)) // Print：4
  println(f.substitute { this[x] = x * y }) // Print：x^2 y^2
  ```

  The following substitutions are supported:

  - Evaluation: Substitute variables into constants
  - Compound expansion: Substitute variables into expressions
  - Substitution: replace the expression with a variable

  > However, it is not yet possible to realize partial substitutions of sums and products: for example, replacing `x + y` from `4 x + 4 y` or replacing `x y` from `x y z`.

- Find the gradient

  To find the gradient, you need to understand another type of object, the vector field `Field`, whose main member is a mapping from a variable to an expression, `{x1 -> f1(x1), x2 -> f2(x2), ... , xn -> fn(xn)}`.
  
  This can be viewed as an n-dimensional vector function whose dimensions are named and composed of expressions. Its input and output are in the variable space `{x1, x2, ... ,xn}`.
  
  The gradient can be calculated after constructing the variable space and scalar field expressions:
  
  ```kotlin
  val space by variableSpace(...)
  val f = ...
  val grad = space.gradientOf(f)
  ```
  
  Vector fields can be transformed modulo into scalar fields:
  
  ```kotlin
  class Field{
      ...
      
      val length = sqrt(expressions.sumBy { (_, e) -> e `^` 2 })
      
      ...
  }
  ```
  
## Solve unconstrained optimization problems

  ### Form

  Unconstrained optimization problems refer to:

  The problem of finding the optimal `x` based on `n` equations `{f<i>(x) == 0 | i ∈ [1,n]}`.

  > where `x` is an `ExpressionVector`, a vector of expressions over `n` dimensions.
  >
  > Named to facilitate partial operations on multiple different variable spaces.

  ### Loss function

  To solve an unconstrained optimisation problem, the first step is to convert equations into a loss function, and convert the problem of finding the optimal solution into the problem of finding the minimum value of the loss function.

  The most commonly used loss function is the mean square error: `e(x) = Σi (f<i>(x))^2 / 2n`.
  
  ### Randomization

  There are 2 ways to use equation:

  - *Batch* - use all leads for each iteration
  
  - *random* - use 1 equation per iteration

  Batch methods are suitable for problems with a small variable space.

  Each iteration of this type of problem is relatively fast, so the main optimisation directions are **improve accuracy** and **reduce the number of iterations**. Using all equations at once can find the globally optimal iteration direction, with less possibility of oscillation.

  Stochastic methods are suitable for problems with a large variable spaces or problems with many similar equations (pathological).

  If the variable space is large, the cost of obtaining all the gradients at once is high, and even for strongly convex problems, it is difficult to obtain a good step size near the extreme point, so it is unlikely to obtain a particularly high-precision solution.

  For two equations that are similar but not identical, batch solving often costs twice as much computational overhead. However, during random optimisation, this cost can be saved by slightly optimising the order of using equations. In addition, problems with a large variable space have a high possibility of pathogenesis.

  The *minimum batch* method, which uses a subset of equations at a time, is a combined form of these two methods.

  For problems that require high precision and a large variable space, the number of equations used in each iteration can be gradually increased over the iterations to obtain the advantages of these solutions at the same time.

  ### direction

  For each iteration, we must first decide the direction to move forward in the variable space. For the problem of finding the extreme value of the loss function, the direction is determined by the first or second order differential of the loss function.

  - First order method - Gradient descent method
  - Second order method - Newton's method

  > For Newton's method, there are the following 2 things to note:
  >
  > - Since the Hessian matrix is ​​required to be invertible, the loss function is required to be related to all dimensions in the variable space, so the randomized iteration is unlikely to apply Newton's method
  > - Newton's method finds the direction to the nearest zero point of the derivative function, which may be the maximum point, minimum point or saddle point. Therefore, the dot product of the Newton direction and the gradient direction is usually calculated. If the dot product is not greater than 0, indicating that the direction found by Newton's method this time is not the gradient descent direction, and there is a high probability that it does not lead to the minimum point. At this time, it can be degraded to the first-order method to ensure stability.

  ### step size

  After deciding the direction, you need to decide the step length in the direction. There are 2 ways to determine the step size:

  - Modification - the step length and direction are obtained from the differential at the same time, usually the module length of the gradient multiplied by a fixed coefficient
  - Re-optimization - After determining the direction, find the directional derivative of the loss function, and then search for the minimum value on the directional derivative.

  ### Common combinations

| method name                | randomization | direction    | step size       |
| :----------:               | :----:        | :--:         | :----:          |
| Gradient Descent           | Batch         | First Order  | Decoration      |
| stochastic gradient descent| stochastic    | first order  | -               |
| Steepest descent           | Batch         | First order  | Re-optimization |
| Newton's method            | Batch         | Second order | Modification    |
| Damped Newton method       | Batch         | Second order | Re-optimization |

  ### Example

  ```kotlin
  // variable space
  val space: VariableSpace
  // equation set
  val samples: List<Expression>
  //Mean square loss function
  val error = samples.map { it `^` 2 / (2 * samples.size) }
  // gradient descent
  batchGD(error.sum(), space) { l -> 1.0 * l }
  // fastest descent
  fastestBatchGD(error.sum(), space)
  // stochastic gradient descent
  stochasticGD(error) { batchGD(it, space) { l -> 1.0 * l } }
  // Randomized steepest descent
  stochasticGD(error) { fastestBatchGD(it, space) }
  // Newton's method
  newton(error.sum(), space)
  // Damped Newton method
  dampingNewton(error.sum(), space)
  ```
  
