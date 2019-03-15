package hu.joti.tuduu.model;

import java.util.Comparator;

public class TaskComparator implements Comparator<Task> {

  private int orderBy;

  public TaskComparator(int orderBy) {
    this.orderBy = orderBy;
  }

  @Override
  public int compare(Task o1, Task o2) {
    switch (orderBy){
      case 1 : { // name
        return ((Task)o1).getName().compareTo(((Task)o2).getName());
      }
      case 2 : { // priority
        return ((Task)o2).getPriority() - ((Task)o1).getPriority();
      }
      case 3 : { // deadline
        String p1 = ((Task)o1).getDeadline();
        String p2 = ((Task)o2).getDeadline();
        if (p1.isEmpty()){
          if (p2.isEmpty())
            return 0;
          else
            return 1;
        } else {
          if (p2.isEmpty())
            return -1;
          else
            return p1.compareTo(p2);
        }
      }
      case 4 : { // id
        return ((Task)o1).getId() - ((Task)o2).getId();
      }
    }
    return 0;
  }
}
