function transducers ()
  "* The final result beforehand: *"
  "** Root mean square of [2, 3, 4, 5] from dirty data **"
  data = [2, 3, 11, 4, 5];
  "*** procedural ***"
  erg=0;
  n=0;
  for x = data
    if x < 10
      n = n + 1;
      erg = erg + x * x;
    end
  end
  sqrt(erg / n)
  "*** functional ***"
  sqrt(mean(data(data < 10).^2))
  "*** fast functional ***"
  xform = comp(filtering(@(x) (x < 10)), ...
               mapping(@sqr));
  sqrt(transduce(xform, @special_mean, data))

  "* Break it down: *"
  v = [2, 3, 4, 5];
  "** Sum of the manually cleaned data [2, 3, 4, 5] **"
  "*** using plain recursion ***"
  sumvec(0, v)
  "*** and the reduce abstraction ***"
  reduce(@summe, 0, v)

  "** Not only the sum but also the number of elements **"
  reduce(@sum_count, struct("sum", 0 , "count", 0), v)

  "** Mean value **"
  "*** our mean function takes zero, one, or two arguments"
  special_mean(reduce(@special_mean, special_mean(), v))
  "*** which lets us create an even better reduce abstraction"
  reduce_better(@special_mean, v)

  "** Sum of the squares **"
  "*** First squaring the data and then summing takes a lot of memory ***"
  reduce(@summe, 0, map(@sqr, v))
  "*** the reduce_better abstraction does not help here ***"
  reduce_better(@summe_complete, map(@sqr, v))
  "*** The high art of functional programming does it in one go ***"
  "**** Understand this and you got the main point of it all ****"
  reduce(apply(mapping(@sqr), (@summe)), 0, v)
  "*** We introduce a further abstraction: transduce"
  transduce(mapping(@sqr), @summe_complete, v)
  "** The mean value again **"
  "*** Sure we can use transduce also for this ***"
  transduce(@identity, @special_mean, v)
  "** Voila, the root mean squared, of the manually cleaned data **"
  sqrt(transduce(mapping(@sqr), @special_mean, v))
  "** Only sum values lower than 4 **"
  transduce(filtering(@(x) (x < 4)), @summe_complete, v)
  "** Sum the squares of values lower than 4 **"
  transduce(comp(filtering(@(x) (x < 4)), ...
                 mapping(@sqr)), ...
            @summe_complete, ...
            v)
  "** The final result shown at the beginning is a peace of cake now **"
  "* THE END *"
end

function erg = first (v)
  erg = v(1);
end

function erg = rest (v)
  erg = v(2:end);
end

function erg = summe (x, y)
  erg = x + y;
end

function erg = sumvec (acc, vals)
  if length(vals) == 0
    erg = acc;
  else
    erg = sumvec(summe(acc, first(vals)), rest(vals));
  end
end

function erg = reduce (f, acc, vals)
  if length(vals) == 0
    erg = acc;
  else
    erg = reduce(f, f(acc, first(vals)), rest(vals));
  end
end

function erg = sum_count (acc, x)
  erg = struct("sum", summe(acc.sum, x), ...
               "count", summe(acc.count, 1));
end

function erg = div (x, y)
  erg = x / y;
end

function erg = special_mean (acc, x)
  if nargin() == 0
    erg = struct("sum", 0 , "count", 0);
  elseif nargin() == 2
    erg = struct("sum", summe(acc.sum, x), ...
                 "count", summe(acc.count, 1));
  elseif nargin() == 1
    erg = div(acc.sum, acc.count);
  end
end

function erg = reduce_better (f, vals)
  erg =  f(reduce(f, f(), vals));
end

function erg = conj (vals, x)
 erg=[vals x];
end

function erg = sqr (x)
  erg = x * x;
end

function erg = map (f, vals)
  erg = reduce(@(acc, x) conj(acc, f(x)), [], vals);
end

function erg = mapping (f)
  erg = @(reducing_function) mapping_helper(reducing_function, f);
end

function erg = mapping_helper (reducing_function, f)
  erg = @(acc, x) reducing_function(acc, f(x));
end

function erg = transduce (xform, f, vals)
 erg = f(reduce(xform(f), f(), vals));
end

function erg = identity (x)
  erg = x;
end

function erg = summe_complete (acc, x)
  if nargin() == 0
    erg = 0;
  elseif nargin() == 2
    erg = acc + x;
  elseif nargin() == 1
    erg = acc;
  end
end

function erg = filtering (pred)
  erg = @(reducing_function) filtering_helper(reducing_function, pred);
end

function erg = filtering_helper (reducing_function, pred)
  erg = @(acc, x) if_for_filter(acc, x, pred, reducing_function);
end

function erg = if_for_filter (acc, x, pred, reducing_function)
 if pred(x)
   erg = reducing_function(acc, x);
 else
   erg = acc;
 end
end

function erg = comp (f, g)
  erg = @(x) f(g(x));
end

function erg = apply (g, y)
  erg = g(y);
end
